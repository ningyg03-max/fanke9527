#!/bin/sh
# SalaryJump 回归测试运行器
#
# 用法：  sh tests/run.sh
#
# 每次运行都会从 work-java/ 重新拉取最新源码编译，
# 所以改完代码直接重跑即可验证，无需手动同步。
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TESTS="$ROOT/tests"
WORK="$ROOT/work-java/com/salaryjump/app"
JAVAC=/usr/lib/jvm/java-17-openjdk/bin/javac
JAVA=/usr/lib/jvm/java-17-openjdk/bin/java

# 被测逻辑类（纯 Java，不依赖 Android UI）
CLASSES="DrinkTypes WaterReminderScheduler WaterReminderSettings WaterHistory WorkdayHelper OvertimeCalc SalaryHistory OvertimeLog StatutoryHolidays HolidayWork CrashLogger HolidayUpdater Contexts LeaveRecord PrivacyLock PaydayReminder WorkRules ExtraIncome ArchiveSync LeaveAccounts CheckInReminder WidgetText SeedBackfill MonthBackfill PetState WorkerPet PetSettings HeroFit SectionSave DayBreakdown DataCache YearlyStats GoalStore BackupPlan PetQuotes RateDisplay"

OUT="$TESTS/out"
FRESH="$TESTS/fresh"

rm -rf "$OUT" "$FRESH"
mkdir -p "$OUT" "$FRESH/com/salaryjump/app"

for c in $CLASSES; do
    if [ ! -f "$WORK/$c.java" ]; then
        echo "错误：缺少源文件 $WORK/$c.java" >&2
        exit 1
    fi
    cp "$WORK/$c.java" "$FRESH/com/salaryjump/app/"
done

echo "编译中……"
$JAVAC -encoding UTF-8 -d "$OUT" \
    $(find "$TESTS/stubs" "$FRESH" "$TESTS/src" -name "*.java")

echo
$JAVA -cp "$OUT" com.salaryjump.app.AllTest
