package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 统计指定时间内的营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end日期内每天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算，计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turnoverList =  new ArrayList<>();
        for (LocalDate date : dateList){
            //查询date日期对应的营业额，营业额是指状态已完成状态的订单就金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumById(map);
            if (turnover == null)turnover = 0.0;
            turnoverList.add(turnover);

        }
        //取出list中每个元素取出来，并加上分隔符
        return TurnoverReportVO.builder()
                                .dateList(StringUtils.join(dateList,","))
                                .turnoverList(StringUtils.join(turnoverList,","))
                                .build();
    }

    /**
     * 用户量统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end日期内每天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算，计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //总用户数量 select count(id) from user where create < ?
        List<Integer> totalUserList = new ArrayList<>();
        //新增用户数量 select count(id) from user where create_time < ? and create_time > ?
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end",endTime);
            //总用户数量
            Integer totalUser = userMapper.countByMap(map);
            map.put("begin",beginTime);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
            newUserList.add(newUser);

        }
        //封装结果数据
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end日期内每天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算，计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        //遍历dateList集合，查询每天有效订单数和订单总数
        for (LocalDate date : dateList) {
            //查询每天订单总数 select count(id) from orders where order_time > ? and order_time < ?
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.from(LocalDateTime.MIN));
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            //查询每天有效订单数 select count(id) from orders where order_time > ? and order_time < ? and status = 5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //计算时间区间内的订单数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount!=0) orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        return OrderReportVO.builder()
                            .dateList(StringUtils.join(dateList, ","))
                            .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                            .orderCountList(StringUtils.join(orderCountList,","))
                            .totalOrderCount(totalOrderCount)
                            .validOrderCount(validOrderCount)
                            .orderCompletionRate(orderCompletionRate)
                            .build();
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end , Integer status){
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end",end);
        map.put("status",status);
        return orderMapper.countByMap(map);
    }

    /**
     * 销量数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getStatusTop10(beginTime,endTime);
        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names,",");
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");
//        封装返回结果数据
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
