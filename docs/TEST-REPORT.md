# 造易 MES — 测试报告

> 生成日期：2026-07-23 | 测试框架：JUnit 5 + Mockito + Spring Boot Test

---

## 概览

| 指标 | 数值 |
|------|------|
| 测试总数 | **53** |
| 通过 | 53 |
| 失败 | 0 |
| 错误 | 0 |
| 跳过 | 0 |
| 测试类数 | 8 |

---

## 测试分类

### 1. 单元测试（Mockito，无 Spring 容器）

#### SysMenuServiceImplTest (6 tests)
| 用例 | 覆盖点 |
|------|--------|
| `testBuildTreeWithNullParentId` | 根节点 parentId=null → 正确构建 2 层树 |
| `testBuildTreeWithZeroParentId` | 旧数据 parentId=0 → 兼容处理 |
| `testBuildTreeEmptyList` | 空列表 → 返回空 |
| `testBuildTreeAllRoot` | 全部根节点 → 3个根，无子节点 |
| `testBuildTreeDeepNesting` | 3 层嵌套 → 正确递归 |
| `testBuildTreeSortOrder` | sort 字段排序 → 正序排列 |

#### SysUserServiceImplTest (8 tests)
| 用例 | 覆盖点 |
|------|--------|
| `login_shouldReturnUserWithToken_whenCredentialsValid` | 正常登录 → 生成 UUID token，清除密码 |
| `login_shouldThrowException_whenUserNotFound` | 用户不存在 → BusinessException |
| `login_shouldThrowException_whenUserDisabled` | 账号禁用 → BusinessException |
| `login_shouldThrowException_whenPasswordWrong` | 密码错误 → BusinessException（BCrypt 不匹配 + MD5 也不匹配） |
| `login_shouldUpdateLastLoginTime` | 登录成功 → 更新 lastLoginTime |
| `pageUsers_shouldCallMapperWithCorrectWrapper` | 分页查询 → 带 keyword 过滤 |
| `assignRoles_shouldDeleteOldAndInsertNew` | 分配角色 → 先删后插，角色 ID 正确 |
| `assignRoles_shouldOnlyDelete_whenRoleIdsNull` | 清空角色 → 仅删除不插入 |
| `assignRoles_shouldOnlyDelete_whenRoleIdsEmpty` | 空数组 → 仅删除不插入 |

#### PasswordEncoderTest (4 tests)
| 用例 | 覆盖点 |
|------|--------|
| `testEncodeAndMatch` | BCrypt 编码 + 匹配 |
| `testDifferentSalts` | 每次编码不同盐值 |
| `testWrongPassword` | 错误密码不匹配 |
| `testEmptyPassword` | 空字符串边界值 |

#### SaleOrderServiceImplTest (6 tests)
| 用例 | 覆盖点 |
|------|--------|
| `create_shouldGenerateOrderNoAndSave` | 创建订单 → 生成 SO 前缀单号，status=1 |
| `create_shouldCalculateTotalAmount` | 多明细 → 金额合计 = item.qty × item.price |
| `update_shouldThrow_whenStatusNotPending` | 非待审核修改 → BusinessException |
| `update_shouldAllowUpdate_whenStatusPending` | 待审核修改 → 通过 |
| `delete_shouldRemoveItemsAndOrder` | 删除 → 先删明细再删主单 |
| `updateStatus_shouldCallMapper` | 状态更新 → 委托 mapper |

#### WorkOrderServiceTest (7 tests)
| 用例 | 覆盖点 |
|------|--------|
| `create_shouldGenerateOrderNoAndSetDefaults` | 创建工单 → WO 前缀，默认值初始化 |
| `startWork_shouldSetStatusToInProgress` | 开工 → status 1→2，记录 actualStart |
| `startWork_shouldThrow_whenNotPending` | 非待生产开工 → BusinessException |
| `complete_shouldSetStatusToFinished` | 完工 → status 2→3，记录 actualEnd |
| `complete_shouldThrow_whenNotInProgress` | 非生产中完工 → BusinessException |
| `finishAndStockIn_shouldSetStatusToStocked` | 入库 → status 3→4 |
| `finishAndStockIn_shouldThrow_whenNotCompleted` | 非已完成入库 → BusinessException |

### 2. 集成测试（@WebMvcTest）

#### AuthControllerTest (4 tests)
| 用例 | 覆盖点 |
|------|--------|
| `login_shouldFail_whenNoCaptcha` | 无验证码 → code=500 |
| `login_shouldFail_whenCaptchaExpired` | 验证码过期 → "验证码已过期" |
| `login_shouldFail_whenCaptchaWrong` | 验证码错误 → "验证码错误" |
| `login_shouldSucceed_whenCredentialsValid` | 完整登录链路 → token + user + 200 |

### 3. 集成测试（@SpringBootTest）

#### Mes1ApplicationTests (1 test)
| 用例 | 覆盖点 |
|------|--------|
| `contextLoads` | Spring 容器启动 |

#### RabbitMQIntegrationTests (17 tests)
| 分类 | 覆盖点 |
|------|--------|
| POJO 序列化 | MqMessage 创建、Jackson 序列化/反序列化、JavaTimeModule（LocalDateTime） |
| 配置验证 | exchange 定义、queue 定义、DLQ 参数、binding、message converter |
| 拓扑一致性 | 名称唯一性、DLQ 不与主队列重叠 |
| 业务负载 | 邮件、审计、工单事件 payload 序列化 |

---

## 覆盖率分析

| 模块 | Service 层测试 | Controller 层测试 | Entity/Mapper |
|------|:--:|:--:|:--:|
| system | ✅ SysUserServiceImpl(8) + SysMenuServiceImpl(6) | ✅ AuthController(4) | ✅ PasswordEncoder(4) |
| sale | ✅ SaleOrderServiceImpl(6) | — | — |
| production | ✅ WorkOrderService(7) | — | — |
| base | — | — | — |
| inventory | — | — | — |
| mq | — | — | ✅ RabbitMQIntegration(17) |

**已覆盖的关键业务流程：**
- 用户登录 + 密码加密 + Token 管理
- 菜单树构建（含边界/回归测试）
- 销售订单创建 + 金额计算 + 状态流转
- 生产工单全生命周期（创建 → 开工 → 完工 → 入库）
- 认证接口 HTTP 层集成测试
- 消息队列序列化 + 配置拓扑

---

## 运行测试

```bash
# 全部测试
./mvnw test

# 单个测试类
./mvnw test -Dtest=SysUserServiceImplTest

# 生成 HTML 报告
./mvnw surefire-report:report
# 报告位置: target/surefire-reports/surefire-report.html
```

---

## 改进建议

1. **补充 Controller 层集成测试**：SaleOrderController、WorkOrderController、SysUserController 的 CRUD 接口
2. **Inventory 模块测试**：目前完全没有覆盖
3. **Dashboard 模块测试**：目前依赖复杂 SQL，建议加集成测试
4. **参数化测试**：`@CsvSource` / `@MethodSource` 减少重复的边界值测试代码
5. **测试覆盖率工具**：接入 JaCoCo 插件生成覆盖率报告
