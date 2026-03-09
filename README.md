# GymLog - 健身训练记录应用

基于 GymLog 二次开发的 Android 健身应用，使用 Kotlin + Jetpack Compose 构建。

## 🎯 核心功能

### 已实现
- ✅ **三分化训练计划导入** - 推/拉/腿经典分化模板
- ✅ **训练计划详情查看** - 动作、组数、次数、重量、休息时长
- ✅ **训练中实时调整** - 可随时修改重量、组数、次数
- ✅ **组间休息计时器** - 可自定义时间，倒计时提醒
- ✅ **训练记录** - 记录每次训练的重量、完成度
- ✅ **纯本地模式** - 使用 Room 数据库，无需 Firebase

### 待实现
- ⏳ 计划导入/导出（JSON 格式）
- ⏳ 训练历史统计图表
- ⏳ 肌肉分布热力图
- ⏳ 后台训练覆盖层服务
- ⏳ 个人最佳（PR）自动检测

## 🛠 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Kotlin |
| **UI** | Jetpack Compose (Material 3) |
| **架构** | MVVM |
| **本地数据库** | Room |
| **导航** | Navigation Compose |
| **异步** | Kotlin Coroutines & Flow |
| **本地存储** | DataStore Preferences |

## 📁 项目结构

```
gym-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/gymlog/app/
│   │   │   ├── data/
│   │   │   │   ├── local/           # 本地数据源
│   │   │   │   │   ├── WorkoutDatabase.kt    # Room 数据库
│   │   │   │   │   ├── WorkoutTemplates.kt   # 默认训练模板
│   │   │   │   │   └── Converters.kt         # 类型转换器
│   │   │   │   ├── model/           # 数据模型
│   │   │   │   │   └── WorkoutModels.kt      # 核心数据类
│   │   │   │   └── repository/      # 数据仓库
│   │   │   │       └── WorkoutRepository.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/         # 屏幕/页面
│   │   │   │   │   ├── PlanListScreen.kt     # 计划列表
│   │   │   │   │   ├── ActiveWorkoutScreen.kt # 训练中界面
│   │   │   │   │   ├── GymLogApp.kt          # 主导航
│   │   │   │   │   └── WorkoutViewModels.kt  # ViewModel
│   │   │   │   └── theme/           # 主题
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Type.kt
│   │   │   ├── util/                # 工具类
│   │   │   └── MainActivity.kt
│   │   ├── res/                     # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 📊 数据模型

### WorkoutPlan (训练计划)
- 三分化训练模板（推/拉/腿）
- 支持自定义计划

### WorkoutDay (训练日)
- 推日：胸部/肩部/三头肌
- 拉日：背部/二头肌/后束
- 腿日：股四头肌/腘绳肌/小腿

### Exercise (训练动作)
- 动作名称、类别、目标肌群
- 默认组数、次数、重量、休息时长

### WorkoutSession (训练记录)
- 实际训练完成情况
- 自动检测个人最佳（PR）

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK 34

### 构建步骤
```bash
# 克隆项目
cd /Users/zuoshiyue/.openclaw/workspace/gym-app

# 使用 Android Studio 打开项目
# 或命令行构建
./gradlew assembleDebug
```

### APK 输出
```
app/build/outputs/apk/debug/app-debug.apk
```

## 📝 使用说明

1. **导入训练计划**
   - 打开应用，点击"+"按钮
   - 选择"导入三分化训练计划"

2. **开始训练**
   - 点击计划卡片查看详情
   - 选择训练日（推/拉/腿）
   - 点击"开始训练"

3. **训练中调整**
   - 修改重量/次数字段
   - 点击✓标记完成
   - 点击"开始休息"启动计时器
   - 点击"下一个"继续

4. **查看历史**
   - 训练完成后自动保存
   - 查看个人最佳记录

## 🔧 开发计划

### Phase 1 (当前)
- [x] 项目初始化
- [x] 数据模型设计
- [x] 本地数据库
- [x] 训练计划 UI
- [x] 训练中界面
- [x] 休息计时器

### Phase 2
- [ ] JSON 导入/导出
- [ ] 训练统计图表
- [ ] 自定义训练计划
- [ ] 动作库搜索

### Phase 3
- [ ] 训练覆盖层服务
- [ ] 数据备份/恢复
- [ ] 主题自定义

## 📄 许可证

MIT License

## 🙏 致谢

原始项目：[GymLog by FedericoCerra](https://github.com/FedericoCerra/GymLog)
动作数据库：[free-exercise-db by yuhonas](https://github.com/yuhonas/free-exercise-db)
