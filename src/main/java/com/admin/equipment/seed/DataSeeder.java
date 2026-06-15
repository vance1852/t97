package com.admin.equipment.seed;

import com.admin.equipment.model.*;
import com.admin.equipment.repo.*;
import com.admin.equipment.security.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepo;
    private final EquipmentRepository equipmentRepo;
    private final WorkOrderRepository workOrderRepo;
    private final InspectionTemplateRepository templateRepo;
    private final InspectionTemplateVersionRepository versionRepo;
    private final InspectionTemplateItemRepository itemRepo;
    private final TemplateBindingRepository bindingRepo;
    private final InspectionOrderRepository orderRepo;
    private final InspectionResultItemRepository resultRepo;
    private final InspectionPlanRepository planRepo;

    @Value("${app.admin-username}")
    private String adminUsername;

    @Value("${app.admin-password}")
    private String adminPassword;

    public DataSeeder(AppUserRepository userRepo, EquipmentRepository equipmentRepo,
                      WorkOrderRepository workOrderRepo, InspectionTemplateRepository templateRepo,
                      InspectionTemplateVersionRepository versionRepo,
                      InspectionTemplateItemRepository itemRepo,
                      TemplateBindingRepository bindingRepo,
                      InspectionOrderRepository orderRepo,
                      InspectionResultItemRepository resultRepo,
                      InspectionPlanRepository planRepo) {
        this.userRepo = userRepo;
        this.equipmentRepo = equipmentRepo;
        this.workOrderRepo = workOrderRepo;
        this.templateRepo = templateRepo;
        this.versionRepo = versionRepo;
        this.itemRepo = itemRepo;
        this.bindingRepo = bindingRepo;
        this.orderRepo = orderRepo;
        this.resultRepo = resultRepo;
        this.planRepo = planRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepo.existsByUsername(adminUsername)) {
            AppUser admin = new AppUser();
            admin.setUsername(adminUsername);
            admin.setPasswordHash(PasswordUtil.hash(adminPassword));
            admin.setDisplayName("平台管理员");
            userRepo.save(admin);
            System.out.println("已创建管理员账号");
        }

        if (equipmentRepo.count() > 0) {
            return;
        }

        Equipment e1 = newEquip("EQ-1001", "一号注塑机", "注塑车间A区", "robot", "normal");
        Equipment e2 = newEquip("EQ-1002", "二号空压机", "动力站", "pump", "warning");
        Equipment e3 = newEquip("EQ-1003", "主输送带", "包装车间", "conveyor", "fault");
        Equipment e4 = newEquip("EQ-1004", "冷却循环水泵", "动力站", "pump", "maintenance");
        equipmentRepo.saveAll(List.of(e1, e2, e3, e4));

        workOrderRepo.saveAll(List.of(
                newOrder(e2.getId(), "空压机压力异常巡检", "inspection", "high", "巡检发现排气压力波动，需排查", "王工", "open"),
                newOrder(e3.getId(), "输送带断带抢修", "repair", "urgent", "包装线输送带断裂，停机抢修", "李工", "in_progress"),
                newOrder(e4.getId(), "循环水泵季度保养", "maintenance", "medium", "按计划做季度保养换油", "张工", "open"),
                newOrder(e1.getId(), "注塑机模具点检", "inspection", "low", "例行模具与液压点检", "赵工", "done")
        ));

        seedInspectionTemplates(e1, e2, e3, e4);

        System.out.println("种子数据初始化完成");
    }

    private void seedInspectionTemplates(Equipment e1, Equipment e2, Equipment e3, Equipment e4) {
        InspectionTemplate pumpTemplate = new InspectionTemplate();
        pumpTemplate.setName("泵类设备日常点检模板");
        pumpTemplate.setDescription("适用于空压机、水泵等泵类设备的日常点检");
        pumpTemplate.setCategory("泵类");
        pumpTemplate.setCurrentVersion(2);
        pumpTemplate.setStatus("active");
        templateRepo.save(pumpTemplate);

        InspectionTemplateVersion pumpV1 = newVersion(pumpTemplate.getId(), 1, "初始版本", "admin");
        versionRepo.save(pumpV1);
        itemRepo.saveAll(List.of(
                newItem(pumpV1.getId(), "pressure", "排气压力", "numeric", "MPa",
                        "检查排气压力是否在正常范围", true, 0, 0.5, 1.2, null, null, null),
                newItem(pumpV1.getId(), "temperature", "运行温度", "numeric", "℃",
                        "检查设备运行温度", true, 1, 20.0, 80.0, null, null, null),
                newItem(pumpV1.getId(), "noise", "异常噪音", "option", "",
                        "是否有异常噪音", true, 2, null, null,
                        "正常,轻微异响,明显异响", "正常", null),
                newItem(pumpV1.getId(), "vibration", "振动情况", "boolean", "",
                        "振动是否在正常范围", true, 3, null, null, null, null, true),
                newItem(pumpV1.getId(), "leakage", "泄漏检查", "boolean", "",
                        "是否有油液或气体泄漏", true, 4, null, null, null, null, false),
                newItem(pumpV1.getId(), "remark", "备注", "text", "",
                        "其他需要记录的事项", false, 5, null, null, null, null, null)
        ));

        InspectionTemplateVersion pumpV2 = newVersion(pumpTemplate.getId(), 2, "增加电流检查项", "admin");
        versionRepo.save(pumpV2);
        itemRepo.saveAll(List.of(
                newItem(pumpV2.getId(), "pressure", "排气压力", "numeric", "MPa",
                        "检查排气压力是否在正常范围", true, 0, 0.5, 1.2, null, null, null),
                newItem(pumpV2.getId(), "temperature", "运行温度", "numeric", "℃",
                        "检查设备运行温度", true, 1, 20.0, 80.0, null, null, null),
                newItem(pumpV2.getId(), "current", "工作电流", "numeric", "A",
                        "检查电机工作电流", true, 2, 5.0, 25.0, null, null, null),
                newItem(pumpV2.getId(), "noise", "异常噪音", "option", "",
                        "是否有异常噪音", true, 3, null, null,
                        "正常,轻微异响,明显异响", "正常", null),
                newItem(pumpV2.getId(), "vibration", "振动情况", "boolean", "",
                        "振动是否在正常范围", true, 4, null, null, null, null, true),
                newItem(pumpV2.getId(), "leakage", "泄漏检查", "boolean", "",
                        "是否有油液或气体泄漏", true, 5, null, null, null, null, false),
                newItem(pumpV2.getId(), "remark", "备注", "text", "",
                        "其他需要记录的事项", false, 6, null, null, null, null, null)
        ));

        InspectionTemplate robotTemplate = new InspectionTemplate();
        robotTemplate.setName("注塑机/机器人点检模板");
        robotTemplate.setDescription("适用于注塑机、工业机器人等设备");
        robotTemplate.setCategory("机器人");
        robotTemplate.setCurrentVersion(1);
        robotTemplate.setStatus("active");
        templateRepo.save(robotTemplate);

        InspectionTemplateVersion robotV1 = newVersion(robotTemplate.getId(), 1, "初始版本", "admin");
        versionRepo.save(robotV1);
        itemRepo.saveAll(List.of(
                newItem(robotV1.getId(), "hydraulic_pressure", "液压系统压力", "numeric", "MPa",
                        "检查液压系统工作压力", true, 0, 10.0, 18.0, null, null, null),
                newItem(robotV1.getId(), "mold_condition", "模具状态", "option", "",
                        "模具外观及安装状态检查", true, 1, null, null,
                        "良好,轻微磨损,需要更换", "良好", null),
                newItem(robotV1.getId(), "safety_door", "安全门功能", "boolean", "",
                        "安全门开关是否正常", true, 2, null, null, null, null, true),
                newItem(robotV1.getId(), "emergency_stop", "急停按钮", "boolean", "",
                        "急停按钮功能是否正常", true, 3, null, null, null, null, true),
                newItem(robotV1.getId(), "cycle_time", "循环周期", "numeric", "s",
                        "生产循环周期", false, 4, 25.0, 45.0, null, null, null),
                newItem(robotV1.getId(), "remark", "备注", "text", "",
                        "其他需要记录的事项", false, 5, null, null, null, null, null)
        ));

        InspectionTemplate conveyorTemplate = new InspectionTemplate();
        conveyorTemplate.setName("输送设备点检模板");
        conveyorTemplate.setDescription("适用于输送带、传送带等输送设备");
        conveyorTemplate.setCategory("输送");
        conveyorTemplate.setCurrentVersion(1);
        conveyorTemplate.setStatus("active");
        templateRepo.save(conveyorTemplate);

        InspectionTemplateVersion conveyorV1 = newVersion(conveyorTemplate.getId(), 1, "初始版本", "admin");
        versionRepo.save(conveyorV1);
        itemRepo.saveAll(List.of(
                newItem(conveyorV1.getId(), "belt_condition", "皮带状态", "option", "",
                        "检查皮带磨损及张紧情况", true, 0, null, null,
                        "正常,轻微磨损,严重磨损,跑偏", "正常", null),
                newItem(conveyorV1.getId(), "roller_rotation", "滚筒转动", "boolean", "",
                        "所有滚筒转动是否灵活", true, 1, null, null, null, null, true),
                newItem(conveyorV1.getId(), "motor_temp", "电机温度", "numeric", "℃",
                        "驱动电机表面温度", true, 2, 20.0, 70.0, null, null, null),
                newItem(conveyorV1.getId(), "abnormal_noise", "异常声响", "boolean", "",
                        "运行中是否有异常声响", true, 3, null, null, null, null, false),
                newItem(conveyorV1.getId(), "remark", "备注", "text", "",
                        "其他需要记录的事项", false, 4, null, null, null, null, null)
        ));

        TemplateBinding b1 = new TemplateBinding();
        b1.setTemplateId(pumpTemplate.getId());
        b1.setEquipmentType("pump");
        bindingRepo.save(b1);

        TemplateBinding b2 = new TemplateBinding();
        b2.setTemplateId(robotTemplate.getId());
        b2.setEquipmentId(e1.getId());
        bindingRepo.save(b2);

        TemplateBinding b3 = new TemplateBinding();
        b3.setTemplateId(conveyorTemplate.getId());
        b3.setEquipmentType("conveyor");
        bindingRepo.save(b3);

        seedInspectionOrders(e1, e2, e4, pumpTemplate, pumpV2, robotTemplate, robotV1, conveyorTemplate, conveyorV1);

        seedInspectionPlans(e2, e4, pumpTemplate);
    }

    private void seedInspectionOrders(Equipment e1, Equipment e2, Equipment e4,
                                      InspectionTemplate pumpTemplate, InspectionTemplateVersion pumpV2,
                                      InspectionTemplate robotTemplate, InspectionTemplateVersion robotV1,
                                      InspectionTemplate conveyorTemplate, InspectionTemplateVersion conveyorV1) {
        LocalDateTime now = LocalDateTime.now();

        InspectionOrder order1 = new InspectionOrder();
        order1.setOrderNo("IO" + now.minusDays(3).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "0001");
        order1.setEquipmentId(e2.getId());
        order1.setTemplateId(pumpTemplate.getId());
        order1.setTemplateVersionId(pumpV2.getId());
        order1.setTemplateVersion(2);
        order1.setStatus("closed");
        order1.setIsPassed(true);
        order1.setExecutor("王工");
        order1.setExecutedAt(now.minusDays(3));
        order1.setClosedAt(now.minusDays(3));
        order1.setSourceType("manual");
        orderRepo.save(order1);

        List<InspectionTemplateItem> pumpItems = itemRepo.findByVersionIdOrderBySortOrderAsc(pumpV2.getId());
        for (InspectionTemplateItem item : pumpItems) {
            InspectionResultItem r = newResultItem(order1.getId(), item);
            if ("pressure".equals(item.getItemKey())) {
                r.setValueNumeric(0.85);
                r.setIsPassed(true);
            } else if ("temperature".equals(item.getItemKey())) {
                r.setValueNumeric(55.0);
                r.setIsPassed(true);
            } else if ("current".equals(item.getItemKey())) {
                r.setValueNumeric(15.5);
                r.setIsPassed(true);
            } else if ("noise".equals(item.getItemKey())) {
                r.setValueText("正常");
                r.setIsPassed(true);
            } else if ("vibration".equals(item.getItemKey())) {
                r.setValueBoolean(true);
                r.setIsPassed(true);
            } else if ("leakage".equals(item.getItemKey())) {
                r.setValueBoolean(false);
                r.setIsPassed(true);
            } else {
                r.setValueText("设备运行正常");
                r.setIsPassed(true);
            }
            r.setIsAbnormal(false);
            resultRepo.save(r);
        }

        InspectionOrder order2 = new InspectionOrder();
        order2.setOrderNo("IO" + now.minusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "0002");
        order2.setEquipmentId(e2.getId());
        order2.setTemplateId(pumpTemplate.getId());
        order2.setTemplateVersionId(pumpV2.getId());
        order2.setTemplateVersion(2);
        order2.setStatus("submitted");
        order2.setIsPassed(false);
        order2.setExecutor("王工");
        order2.setExecutedAt(now.minusDays(1));
        order2.setSourceType("manual");
        orderRepo.save(order2);

        WorkOrder wo1 = newOrder(e2.getId(), "点检异常: 排气压力超标", "repair", "high",
                "点检发现排气压力1.5MPa，超过正常范围上限1.2MPa，需立即检修", "李工", "in_progress");
        workOrderRepo.save(wo1);

        int idx = 0;
        for (InspectionTemplateItem item : pumpItems) {
            InspectionResultItem r = newResultItem(order2.getId(), item);
            r.setSortOrder(idx++);
            if ("pressure".equals(item.getItemKey())) {
                r.setValueNumeric(1.5);
                r.setIsPassed(false);
                r.setIsAbnormal(true);
                r.setAbnormalRemark("压力偏高，超过正常范围上限，需检修");
                r.setWorkOrderId(wo1.getId());
            } else if ("temperature".equals(item.getItemKey())) {
                r.setValueNumeric(72.0);
                r.setIsPassed(true);
            } else if ("current".equals(item.getItemKey())) {
                r.setValueNumeric(22.0);
                r.setIsPassed(true);
            } else if ("noise".equals(item.getItemKey())) {
                r.setValueText("轻微异响");
                r.setIsPassed(false);
                r.setIsAbnormal(true);
                r.setAbnormalRemark("有轻微异响，可能是轴承问题");
            } else if ("vibration".equals(item.getItemKey())) {
                r.setValueBoolean(true);
                r.setIsPassed(true);
            } else if ("leakage".equals(item.getItemKey())) {
                r.setValueBoolean(false);
                r.setIsPassed(true);
            } else {
                r.setValueText("发现压力异常，已报修");
                r.setIsPassed(true);
            }
            resultRepo.save(r);
        }

        InspectionOrder order3 = new InspectionOrder();
        order3.setOrderNo("IO" + now.minusDays(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "0003");
        order3.setEquipmentId(e1.getId());
        order3.setTemplateId(robotTemplate.getId());
        order3.setTemplateVersionId(robotV1.getId());
        order3.setTemplateVersion(1);
        order3.setStatus("closed");
        order3.setIsPassed(true);
        order3.setExecutor("赵工");
        order3.setExecutedAt(now.minusDays(2));
        order3.setClosedAt(now.minusDays(2));
        order3.setSourceType("manual");
        orderRepo.save(order3);

        List<InspectionTemplateItem> robotItems = itemRepo.findByVersionIdOrderBySortOrderAsc(robotV1.getId());
        int ridx = 0;
        for (InspectionTemplateItem item : robotItems) {
            InspectionResultItem r = newResultItem(order3.getId(), item);
            r.setSortOrder(ridx++);
            if ("hydraulic_pressure".equals(item.getItemKey())) {
                r.setValueNumeric(14.5);
                r.setIsPassed(true);
            } else if ("mold_condition".equals(item.getItemKey())) {
                r.setValueText("良好");
                r.setIsPassed(true);
            } else if ("safety_door".equals(item.getItemKey())) {
                r.setValueBoolean(true);
                r.setIsPassed(true);
            } else if ("emergency_stop".equals(item.getItemKey())) {
                r.setValueBoolean(true);
                r.setIsPassed(true);
            } else if ("cycle_time".equals(item.getItemKey())) {
                r.setValueNumeric(32.0);
                r.setIsPassed(true);
            } else {
                r.setValueText("");
                r.setIsPassed(true);
            }
            resultRepo.save(r);
        }

        InspectionOrder order4 = new InspectionOrder();
        order4.setOrderNo("IO" + now.minusHours(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "0004");
        order4.setEquipmentId(e4.getId());
        order4.setTemplateId(pumpTemplate.getId());
        order4.setTemplateVersionId(pumpV2.getId());
        order4.setTemplateVersion(2);
        order4.setStatus("draft");
        order4.setIsPassed(null);
        order4.setExecutor("张工");
        order4.setSourceType("manual");
        orderRepo.save(order4);

        int pidx = 0;
        for (InspectionTemplateItem item : pumpItems) {
            InspectionResultItem r = newResultItem(order4.getId(), item);
            r.setSortOrder(pidx++);
            if ("pressure".equals(item.getItemKey())) {
                r.setValueNumeric(0.7);
                r.setIsPassed(true);
            } else if ("temperature".equals(item.getItemKey())) {
                r.setValueNumeric(45.0);
                r.setIsPassed(true);
            }
            resultRepo.save(r);
        }
    }

    private void seedInspectionPlans(Equipment e2, Equipment e4, InspectionTemplate pumpTemplate) {
        InspectionPlan plan1 = new InspectionPlan();
        plan1.setName("动力站泵类每日点检");
        plan1.setTemplateId(pumpTemplate.getId());
        plan1.setEquipmentType("pump");
        plan1.setCycleType("daily");
        plan1.setCycleValue(1);
        plan1.setExecutor("王工");
        plan1.setStatus("active");
        plan1.setNextRunAt(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));
        planRepo.save(plan1);

        InspectionPlan plan2 = new InspectionPlan();
        plan2.setName("二号空压机专项点检");
        plan2.setTemplateId(pumpTemplate.getId());
        plan2.setEquipmentIds(e2.getId().toString());
        plan2.setCycleType("hourly");
        plan2.setCycleValue(4);
        plan2.setExecutor("王工");
        plan2.setStatus("active");
        plan2.setNextRunAt(LocalDateTime.now().plusHours(4));
        planRepo.save(plan2);
    }

    private Equipment newEquip(String code, String name, String location, String type, String status) {
        Equipment e = new Equipment();
        e.setCode(code);
        e.setName(name);
        e.setLocation(location);
        e.setType(type);
        e.setStatus(status);
        return e;
    }

    private WorkOrder newOrder(Long equipmentId, String title, String type, String priority,
                               String description, String assignee, String status) {
        WorkOrder w = new WorkOrder();
        w.setEquipmentId(equipmentId);
        w.setTitle(title);
        w.setType(type);
        w.setPriority(priority);
        w.setDescription(description);
        w.setAssignee(assignee);
        w.setStatus(status);
        return w;
    }

    private InspectionTemplateVersion newVersion(Long templateId, int version,
                                                 String changeLog, String createdBy) {
        InspectionTemplateVersion v = new InspectionTemplateVersion();
        v.setTemplateId(templateId);
        v.setVersion(version);
        v.setChangeLog(changeLog);
        v.setCreatedBy(createdBy);
        return v;
    }

    private InspectionTemplateItem newItem(Long versionId, String key, String name, String type,
                                           String unit, String prompt, boolean required, int sortOrder,
                                           Double numMin, Double numMax,
                                           String optionValues, String passOptions, Boolean boolPass) {
        InspectionTemplateItem item = new InspectionTemplateItem();
        item.setVersionId(versionId);
        item.setItemKey(key);
        item.setName(name);
        item.setType(type);
        item.setUnit(unit);
        item.setPrompt(prompt);
        item.setRequired(required);
        item.setSortOrder(sortOrder);
        item.setNumericMin(numMin);
        item.setNumericMax(numMax);
        item.setOptionValues(optionValues);
        item.setPassOptions(passOptions);
        item.setBooleanPassValue(boolPass);
        return item;
    }

    private InspectionResultItem newResultItem(Long orderId, InspectionTemplateItem templateItem) {
        InspectionResultItem r = new InspectionResultItem();
        r.setOrderId(orderId);
        r.setItemId(templateItem.getId());
        r.setItemKey(templateItem.getItemKey());
        r.setItemName(templateItem.getName());
        r.setItemType(templateItem.getType());
        r.setSortOrder(templateItem.getSortOrder());
        r.setIsPassed(null);
        r.setIsAbnormal(false);
        return r;
    }
}
