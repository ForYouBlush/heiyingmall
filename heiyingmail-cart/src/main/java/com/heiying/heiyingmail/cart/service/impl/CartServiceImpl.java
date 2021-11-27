package com.heiying.heiyingmail.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.cart.feign.ProductFeignService;
import com.heiying.heiyingmail.cart.interceptor.CartInterceptor;
import com.heiying.heiyingmail.cart.service.CartService;
import com.heiying.heiyingmail.cart.to.UserInfoTO;
import com.heiying.heiyingmail.cart.vo.Cart;
import com.heiying.heiyingmail.cart.vo.CartItem;
import com.heiying.heiyingmail.cart.vo.SkuInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    private final String CART_PREFIX = "heiyingmail:cart:";


    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String o = (String) cartOps.get(skuId.toString());
        if (!StringUtils.isEmpty(o)) {
            //购物车中有这个商品，修改数量、总价即可
            CartItem item = JSON.parseObject(o, CartItem.class);
            item.setCount(item.getCount() + num);
//            BigDecimal total = item.getTotalPrice().add(item.getPrice().multiply(new BigDecimal("" + item.getCount())));
//            item.setTotalPrice(total);
            cartOps.put(skuId.toString(), JSON.toJSONString(item));
            return item;
        }

        // 远程查询当前要添加的商品的信息
        R info = productFeignService.getSkuInfo(skuId);
        SkuInfoVO data = info.getData("skuInfo", new TypeReference<SkuInfoVO>() {
        });
        //添加新商品到购物车
        CartItem cartItem = new CartItem();
        CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setImage(data.getSkuDefaultImg());
            cartItem.setTitle(data.getSkuTitle());
            cartItem.setSkuId(skuId);
            cartItem.setPrice(data.getPrice());
        }, executor);


        //远程查询sku的组合信息
        CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
            List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(attrValues);
        }, executor);

        CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);

        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(o, CartItem.class);
        return cartItem;
    }

    /**
     * 获取整个购物车
     *
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTO userInfoTO = CartInterceptor.threadLocal.get();
        if (userInfoTO.getUserId() != null) {
            //登录状态
            String userKey = CART_PREFIX + userInfoTO.getUserId();
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(userKey);
            //如果临时购物车的数据还没有合并，则进行合并
            List<CartItem> tempCarts = getCartItems(CART_PREFIX + userInfoTO.getUserKey());
            if (tempCarts != null && tempCarts.size() > 0) {
                //临时购物车有数据，需要合并
                for (CartItem tempCart : tempCarts) {
                    addToCart(tempCart.getSkuId(), tempCart.getCount());
                }
                //清除临时购物车的数据
                clearCart(CART_PREFIX + userInfoTO.getUserKey());
            }
            //登陆后的所有购物车【包含合并后的临时购物车数据，和登陆后的购物车数据】
            List<CartItem> items = getCartItems(userKey);
            cart.setItems(items);
        } else {
            //没登陆
            String userKey = CART_PREFIX + userInfoTO.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(userKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 获取到我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTO userInfoTO = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTO.getUserId() != null) {
            //登录了key为用户的id
            cartKey = CART_PREFIX + userInfoTO.getUserId();
        } else {
            //没登陆key为临时用户的user-key
            cartKey = CART_PREFIX + userInfoTO.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        return hashOps;
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        List<CartItem> collect = null;
        if (values != null && values.size() > 0) {
            collect = values.stream().map(obj -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
        }
        return collect;
    }

    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void deleteItem(Long deleteId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(deleteId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserItems() {
        UserInfoTO userInfoTO = CartInterceptor.threadLocal.get();
        if (userInfoTO.getUserId() == null) {
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoTO.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            //获取所有被选中的购物项
            List<CartItem> collect = cartItems.stream().filter(item -> item.getCheck())
                    .map(cartItem -> {
                        //更新最新价格
                        BigDecimal price = productFeignService.getPrice(cartItem.getSkuId());
                        cartItem.setPrice(price);
                        return cartItem;
                    }).collect(Collectors.toList());
            return collect;
        }
    }
}
