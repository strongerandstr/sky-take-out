package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        StringBuilder dateList = new StringBuilder();
        StringBuilder turnoverList = new StringBuilder();
        LocalDate curr = begin;
        while(!curr.equals(end.plusDays(1L))){
            dateList.append(curr.toString() + ',');
            BigDecimal turnover = orderMapper.getTurnover(curr, Orders.COMPLETED);
            if(turnover == null){
                turnover = BigDecimal.ZERO;
            }
            turnoverList.append(turnover.toString() + ',');
            curr = curr.plusDays(1L);
        }
        dateList.deleteCharAt(dateList.length()-1);
        turnoverList.deleteCharAt(turnoverList.length()-1);

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(dateList.toString());
        turnoverReportVO.setTurnoverList(turnoverList.toString());
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        StringBuilder dateList = new StringBuilder();
        StringBuilder totalUserList = new StringBuilder();
        StringBuilder newUserList = new StringBuilder();

        LocalDateTime prev = LocalDateTime.MIN;
        LocalDateTime lastDay = LocalDateTime.of(begin.minusDays(1), LocalTime.MAX);
        BigDecimal totalUser = userMapper.getUserNum(prev, lastDay);
        totalUser = totalUser == null ? BigDecimal.ZERO : totalUser;

        LocalDate curr = begin;
        while(!curr.equals(end.plusDays(1L))){
            LocalDateTime zero = LocalDateTime.of(curr, LocalTime.MIN);
            LocalDateTime midnight = LocalDateTime.of(curr, LocalTime.MAX);

            dateList.append(curr.toString() + ',');
            BigDecimal todayUser = userMapper.getUserNum(zero, midnight);
            if(todayUser == null){
                totalUser = BigDecimal.ZERO;
            }
            totalUser = todayUser.add(totalUser);
            newUserList.append(todayUser.toString() + ',');
            totalUserList.append(totalUser.toString() + ',');
            curr = curr.plusDays(1L);
        }

        dateList.deleteCharAt(dateList.length()-1);
        totalUserList.deleteCharAt(totalUserList.length()-1);
        newUserList.deleteCharAt(newUserList.length()-1);

        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(dateList.toString());
        userReportVO.setNewUserList(newUserList.toString());
        userReportVO.setTotalUserList(totalUserList.toString());
        return userReportVO;
    }

    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);

        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList = orderDetailMapper.getSalesTop10(beginTime, endTime);

        String nameList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()),",");
        String numberList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()),",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end,LocalTime.MAX));

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try{
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("sheet1");
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);
            XSSFRow row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date,LocalTime.MAX));
                row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());


            }
            ServletOutputStream out = httpServletResponse.getOutputStream();
            excel.write(out);
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap();
        map.put("status", status);
        map.put("begin",beginTime);
        map.put("end", endTime);
        return orderMapper.countByMap(map);
    }
}
