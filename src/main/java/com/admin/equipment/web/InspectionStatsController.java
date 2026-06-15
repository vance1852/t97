package com.admin.equipment.web;

import com.admin.equipment.service.InspectionStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/inspection-stats")
public class InspectionStatsController {

    private final InspectionStatsService statsService;

    public InspectionStatsController(InspectionStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        return statsService.getOverview();
    }

    @GetMapping("/pass-rate")
    public Map<String, Object> getPassRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long templateId) {
        return statsService.getPassRateStats(startTime, endTime, equipmentId, templateId);
    }

    @GetMapping("/abnormal-distribution")
    public Map<String, Object> getAbnormalDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return statsService.getAbnormalDistribution(startTime, endTime);
    }

    @GetMapping("/closure-rate")
    public Map<String, Object> getClosureRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return statsService.getClosureRateStats(startTime, endTime);
    }

    @GetMapping("/equipment/{equipmentId}")
    public Map<String, Object> getEquipmentStats(@PathVariable Long equipmentId) {
        return statsService.getEquipmentStats(equipmentId);
    }

    @GetMapping("/template/{templateId}")
    public Map<String, Object> getTemplateStats(@PathVariable Long templateId) {
        return statsService.getTemplateStats(templateId);
    }
}
