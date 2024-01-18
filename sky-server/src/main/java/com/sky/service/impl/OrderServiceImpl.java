package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShopppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShopppingCartMapper shopppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO) {
        //处理各种业务异常（地址簿为空，数据车数据为空）
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(userId);
        addressBook.setId(orderSubmitDTO.getAddressBookId());
        AddressBook addressBook1 = addressBookMapper.getById(addressBook);
        if(addressBook1 == null){
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询当前用户的购物车数据
        ShoppingCart shoppingCart =  new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shopppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook1.getPhone());
        orders.setConsignee(addressBook1.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart: shoppingCartList
             ) {
            OrderDetail orderDetail = new OrderDetail();//订单明细
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的id
            //批量插入
            orderDetailList.add(orderDetail);
        }
        //批量插入订单明细数据
        orderDetailMapper.insertBatch(orderDetailList);
        //清空用户购物车
        shopppingCartMapper.deleteByUserId(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();


        return orderSubmitVO;
    }
}
