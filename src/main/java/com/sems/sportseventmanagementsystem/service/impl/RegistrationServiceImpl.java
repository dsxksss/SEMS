package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.model.dto.RegistrationDTO;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.repository.RegistrationRepository;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.model.entity.Registration;
import com.sems.sportseventmanagementsystem.service.RegistrationService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Override
    @Transactional
    public Registration register(Long userId, RegistrationDTO registrationDTO) {
        // 查询赛事
        Event event = eventRepository.findById(registrationDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("赛事不存在"));

        // 检查赛事状态
        if (!"OPEN".equals(event.getStatus())) {
            throw new ResourceNotFoundException("赛事已结束，无法报名");
        }

        // 检查人数是否已满
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new ResourceNotFoundException("赛事报名人数已满");
        }

        // 检查用户是否已报名
        Optional<Registration> existingRegistration = registrationRepository.findByUserIdAndEventId(userId,
                registrationDTO.getEventId());
        if (existingRegistration.isPresent()) {
            throw new ResourceNotFoundException("您已报名此赛事");
        }

        // 创建报名记录
        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setEventId(registrationDTO.getEventId());
        registration.setRegistrationType(registrationDTO.getRegistrationType());
        registration.setStatus("PENDING");
        registration.setRemark(registrationDTO.getRemark());
        registration.setRegisterTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());

        registrationRepository.save(registration);

        // 更新赛事人数
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        return registration;
    }

    @Override
    @Transactional
    public boolean cancelRegistration(Long userId, Long registrationId) {
        // 查询报名记录
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("报名记录不存在"));

        // 检查是否是用户自己的报名
        if (!registration.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("无权操作此报名记录");
        }

        // 查询赛事
        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("赛事不存在"));

        // 检查赛事状态
        if ("ENDED".equals(event.getStatus())) {
            throw new ResourceNotFoundException("赛事已结束，无法取消报名");
        }

        // 删除报名记录
        registrationRepository.deleteById(registrationId);

        // 更新赛事人数
        event.setCurrentParticipants(event.getCurrentParticipants() - 1);
        eventRepository.save(event);

        return true;
    }

    @Override
    @Transactional
    public Registration reviewRegistration(Long registrationId, String status, String remark) {
        // 查询报名记录
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("报名记录不存在"));

        // 更新状态和备注
        registration.setStatus(status);
        registration.setRemark(remark);
        registration.setUpdateTime(LocalDateTime.now());

        registrationRepository.save(registration);

        // 如果拒绝报名，需要减少赛事人数
        if ("REJECTED".equals(status)) {
            Event event = eventRepository.findById(registration.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("赛事不存在"));
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
            eventRepository.save(event);
        }

        return registration;
    }

    @Override
    public List<Registration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    @Override
    public List<Registration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    @Override
    public Registration getRegistrationById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("报名记录不存在"));
    }

    @Override
    public List<Registration> getRegistrationsByStatus(String status) {
        return registrationRepository.findByStatus(status);
    }

    @Override
    public byte[] exportRegistrations(Long eventId) {
        // 获取赛事报名列表
        List<Registration> registrations = getEventRegistrations(eventId);

        // 导出为Excel
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 创建工作簿
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                // 创建工作表
                XSSFSheet sheet = workbook.createSheet("报名信息");

                // 设置表头
                String[] headers = { "报名ID", "用户ID", "用户名", "赛事ID", "赛事名称", "报名时间", "状态", "备注" };
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // 填充数据
                int rowNum = 1;
                for (Registration registration : registrations) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(registration.getId());
                    row.createCell(1).setCellValue(registration.getUserId());
                    row.createCell(2)
                            .setCellValue(registration.getUser() != null ? registration.getUser().getUsername() : "");
                    row.createCell(3).setCellValue(registration.getEventId());
                    row.createCell(4)
                            .setCellValue(registration.getEvent() != null ? registration.getEvent().getName() : "");
                    row.createCell(5).setCellValue(registration.getRegisterTime().toString());
                    row.createCell(6).setCellValue(registration.getStatus());
                    row.createCell(7).setCellValue(registration.getRemark() != null ? registration.getRemark() : "");
                }

                // 自动调整列宽
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 写入输出流
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("导出报名数据失败", e);
            throw new RuntimeException("导出报名数据失败", e);
        }
    }
}