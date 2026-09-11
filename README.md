# LUMEN — Smart Camera Notebook

**AI-Powered Visual Knowledge & Document Intelligence System for Android**

Kotlin · Jetpack Compose · Material 3 · CameraX · ML Kit · Room · Hilt · WorkManager · Coroutines & Flow

This repository contains three README sections, one per supported app language: **English**, **فارسی (Persian)**, and **中文 (Chinese)**. Jump to the section you need:

- [🇬🇧 English](#-english)
- [🇮🇷 فارسی](#-فارسی)
- [🇨🇳 中文](#-中文)

---

## 🇬🇧 English

### What is LUMEN?

LUMEN turns your phone's camera into a visual knowledge system. Point it at a whiteboard, a book page, a receipt, a table, a form, a business card, or a QR code, and LUMEN recognises the content, runs on-device OCR, and turns it into something structured and searchable: a **Note, Document, Table, Task, Reminder, Receipt, Flashcard, Summary, Quote, Contact,** or **Event.**

The app is **offline-first**: camera capture, on-device OCR (Google ML Kit), local database storage (Room + FTS full-text search), table/receipt/business-card parsing, summarisation, flashcard generation, PDF export and biometric locking all work without an internet connection. Any cloud AI or cloud translation provider is strictly **opt-in** and gated behind an explicit consent toggle in Settings.

### Key features implemented in this codebase

- **Smart Camera Scanner** — real CameraX pipeline (preview, autofocus, torch, zoom, adaptive frame-rate Live-OCR sampling)
- **Document processing pipeline** — Capture → Preprocess → Detect → OCR → Structure → Clean → Analyze → Summarize → Index → Save, with per-stage progress
- **On-device OCR** via Google ML Kit (Latin + Chinese script models), with per-line confidence estimation
- **Perspective correction, deskew, contrast enhancement** for document flattening
- **Table Intelligence Engine** — heuristic row/column clustering from OCR bounding boxes into an editable table (CSV / JSON / Markdown export)
- **Receipt & Invoice parser** — regex/heuristic extraction of merchant, date, total, tax, currency, payment method, and line items, each traceable back to its source OCR line
- **Business Card parser** — name/company/title/phone/email/website/address extraction, saved to Contacts only after explicit confirmation
- **Form field (Key/Value) extractor**
- **QR / Barcode scanner** via ML Kit Barcode Scanning
- **On-device extractive summarisation** (Short / Medium / Deep) and **keyword extraction**
- **Flashcard generator** from Q/A and definition patterns in scanned text
- **Date/deadline/action-item extraction** → Tasks, Reminders, Calendar Events (added only after confirmation)
- **Smart Duplicate Detection** via a real perceptual-hash (aHash) implementation
- **AI Cleanup Engine** — deterministic OCR-artifact cleanup that always preserves the original text
- **Notebook / Workspace system** — folders, tags, favourites, pin, archive
- **Local full-text Search Engine** (Room FTS4) across notes, documents and receipts
- **PDF Studio** (multi-page PDF assembly from scanned pages) and **CSV / JSON / Markdown / TXT export**
- **Encrypted local Backup & Restore** — AES/GCM via Android Keystore, zipped database + images
- **Biometric App/Notebook Lock** via BiometricPrompt
- **Storage Manager** — real on-disk breakdown (originals, processed, OCR data, PDFs, thumbnails, cache) with safe "Clear cache" (never touches originals)
- **Battery & Performance Manager** — adapts Live-OCR sampling rate and batch-job parallelism to battery level / thermal throttling
- **Business Hours module** — a fully user-editable operating-hours screen (see below) with live open/closed status and countdown
- **Five themes**: Windows 11 (default), Light, Dark, Red, Blue — togglable in Settings
- **Three languages**: English (LTR), فارسی/Persian (RTL), 中文/Chinese (LTR), with per-app language switching and full `LayoutDirection` handling
- **WorkManager background jobs** for OCR processing, batch scanning, and backups (Hilt-injected `CoroutineWorker`s)

### The Business Hours module

Under **Settings → Business hours**, you create one or more schedules from scratch: a label, an IANA time zone (e.g. `Europe/Istanbul`), and — for each day of the week — whether it's open, and its open/close time. Nothing is pre-filled. LUMEN then computes, live:

- whether it's **open now** or **closed**
- the exact **time it opens/closes next**
- the **duration remaining** until that change (e.g. "2h 15m")

Overnight schedules (e.g. open 18:00, close 02:00) are supported.

### Honest scope notes (please read before filing issues)

This is a full, real, from-scratch Android codebase (Clean Architecture + MVVM, ~110 Kotlin files, ~6,000 lines) built to be genuinely functional rather than a mockup — every feature listed above calls real Android/ML Kit/Room/WorkManager/Compose APIs, not placeholder stubs. That said, in the interest of not overselling automated capabilities the underlying libraries genuinely don't provide, a few areas are intentionally implemented as **clearly-labelled heuristics** rather than as trained ML models, and the code comments say so at the point of implementation:

| Area | What's implemented | Limitation |
|---|---|---|
| Persian/Arabic-script OCR | ML Kit OCR wrapper with pluggable script selection | Google ML Kit's on-device text recognizer does not currently ship an Arabic/Persian script model (only Latin, Chinese, Japanese, Korean, Devanagari). Persian-script text will OCR poorly out of the box; a cloud OCR provider is the documented extension point. |
| Document edge auto-detection | A lightweight luminance-gradient heuristic | Not a full contour/Hough pipeline; the UI always allows manual corner drag-correction |
| Table extraction | Bounding-box row/column clustering | Works well on grid-like tables; complex/merged-cell tables need manual correction (fully supported in the Table Detail screen) |
| Handwriting recognition | Standard OCR + low-confidence flagging | No dedicated HTR model is bundled; messy handwriting will show as low-confidence/needs-review |
| Chart/graph reading, formula/math recognition | Basic symbol substitution / stubs, clearly marked "approximate" | Not a trained model; treat as a starting point, not a finished feature |
| Summarisation & flashcards | Real on-device extractive NLP (word-frequency scoring, Q/A pattern detection) | Not an LLM; an optional Cloud AI provider interface is included for teams that want to plug one in |
| "Searchable" PDF text layer | Multi-page image PDF assembly (Android `PdfDocument`) | No invisible OCR text layer yet — documented as an extension point |

None of this is hidden inside the code — every simplification above has an explicit comment in the relevant file explaining the real scope and pointing to the extension point.

### Project structure

```
LUMEN/
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/lumen/app/
│       ├── camera/          CameraX pipeline
│       ├── core/             theme, navigation, locale, utils (battery, storage, dates, images)
│       ├── data/              Room entities, DAOs, database, repository implementations
│       ├── di/                 Hilt modules
│       ├── domain/          models, repository interfaces, use cases
│       ├── export/          PDF / CSV / JSON / Markdown / TXT exporters
│       ├── intelligence/  table/receipt/business-card/form/date/summary/flashcard/translation engines
│       ├── ocr/               Google ML Kit OCR wrapper
│       ├── security/       Android Keystore crypto + BiometricPrompt
│       ├── vision/          image preprocessing, edge detection, perceptual hash, barcode scanning
│       ├── workers/       WorkManager CoroutineWorkers
│       └── ui/                Compose screens + ViewModels, per feature
│   └── src/main/res/       strings (en/fa/zh), themes, colors, launcher icon
│   └── src/test/               unit tests (business hours calculation, table extraction, perceptual hash)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### Prerequisites

- **Android Studio** Koala (2024.1) or newer
- **JDK 17**
- **Android SDK** Platform 34, Build-Tools 34
- A physical device or emulator running **Android 8.0 (API 26) or newer** with a camera

### Installation & build

1. Open the project folder in Android Studio (`File → Open…`) and let it sync Gradle.
2. Or build from the command line:

```bash
# From the project root
./gradlew assembleDebug
```

If you don't have the Gradle wrapper jar (`gradle/wrapper/gradle-wrapper.jar` is not bundled in this archive to keep it small), generate it once with a locally installed Gradle:

```bash
gradle wrapper --gradle-version 8.7
```

3. Install the resulting APK on a connected device/emulator:

```bash
./gradlew installDebug
```

4. Run unit tests:

```bash
./gradlew test
```

### Required library / SDK components

All of the following are declared in `app/build.gradle.kts` and will be fetched automatically by Gradle — no manual downloads needed beyond having internet access during the first build:

```
androidx.core:core-ktx
androidx.appcompat:appcompat
androidx.exifinterface:exifinterface
androidx.compose (BOM) + material3 + navigation-compose
com.google.dagger:hilt-android (+ compiler)
androidx.hilt:hilt-navigation-compose, hilt-work (+ compiler)
androidx.room:room-runtime/ktx (+ compiler)
androidx.work:work-runtime-ktx
androidx.datastore:datastore-preferences
androidx.camera:camera-core/camera2/lifecycle/view
com.google.mlkit:text-recognition, text-recognition-chinese, barcode-scanning
org.tensorflow:tensorflow-lite, tensorflow-lite-support
androidx.biometric:biometric
androidx.security:security-crypto
com.squareup.retrofit2:retrofit (+ kotlinx-serialization converter), okhttp
io.coil-kt:coil-compose
```

### Permissions requested

Camera, (optional) microphone, notifications, contacts, calendar, and network access — each is requested only when the corresponding feature is used, and every permission's purpose is explained in the in-app Privacy Center.

### License

This starter codebase is provided under the MIT License. See below for the license text if you add a `LICENSE` file to your repository.

---

## 🇮🇷 فارسی

### لومن چیست؟

لومن دوربین گوشی شما را به یک سیستم دانش تصویری تبدیل می‌کند. دوربین را به سمت تخته، صفحه کتاب، رسید، جدول، فرم، کارت ویزیت یا کد QR بگیرید؛ لومن محتوا را تشخیص می‌دهد، تشخیص متن (OCR) را روی خود دستگاه اجرا می‌کند و آن را به یک قالب ساختاریافته و قابل جستجو تبدیل می‌کند: **یادداشت، سند، جدول، وظیفه، یادآور، رسید، فلش‌کارت، خلاصه، نقل‌قول، مخاطب** یا **رویداد**.

این اپلیکیشن **Offline-First** است: گرفتن عکس، تشخیص متن روی دستگاه (Google ML Kit)، ذخیره‌سازی محلی (Room همراه با جستجوی تمام‌متن FTS)، تجزیه جدول/رسید/کارت‌ویزیت، خلاصه‌سازی، تولید فلش‌کارت، خروجی PDF و قفل بیومتریک، همگی بدون نیاز به اینترنت کار می‌کنند. هرگونه هوش مصنوعی یا ترجمه ابری کاملاً **اختیاری** است و فقط پس از رضایت صریح در تنظیمات فعال می‌شود.

### ویژگی‌های کلیدی پیاده‌سازی‌شده در این کدبیس

- **اسکنر هوشمند دوربین** — پایپ‌لاین واقعی CameraX (پیش‌نمایش، فوکوس خودکار، فلاش، زوم، نرخ نمونه‌برداری تطبیقی برای OCR زنده)
- **پایپ‌لاین پردازش سند** — گرفتن → پیش‌پردازش → تشخیص → OCR → ساختاردهی → پاکسازی → تحلیل → خلاصه‌سازی → ایندکس → ذخیره، با پیشرفت مستقل هر مرحله
- **OCR روی دستگاه** با Google ML Kit (مدل‌های لاتین و چینی) همراه با برآورد اعتماد هر خط
- **اصلاح پرسپکتیو، صاف‌سازی، افزایش کنتراست** برای تخت‌سازی سند
- **موتور هوشمند جدول** — خوشه‌بندی ابتکاری سطر/ستون از روی جعبه‌های محدودکننده OCR به یک جدول قابل ویرایش (خروجی CSV، JSON، Markdown)
- **تجزیه‌گر رسید و فاکتور** — استخراج ابتکاری/مبتنی بر عبارت‌باقاعده از فروشنده، تاریخ، جمع کل، مالیات، ارز، روش پرداخت و اقلام، هرکدام قابل ردیابی تا خط OCR منبع
- **تجزیه‌گر کارت ویزیت** — استخراج نام/شرکت/سمت/تلفن/ایمیل/وب‌سایت/آدرس، ذخیره در مخاطبین فقط پس از تأیید صریح کاربر
- **استخراج‌کننده فیلدهای فرم (کلید/مقدار)**
- **اسکنر QR / بارکد** با ML Kit
- **خلاصه‌سازی استخراجی روی دستگاه** (کوتاه/متوسط/عمیق) و **استخراج کلمات کلیدی**
- **تولید فلش‌کارت** از الگوهای پرسش/پاسخ و تعریف در متن اسکن‌شده
- **استخراج تاریخ/مهلت/آیتم اقدام** → وظیفه، یادآور، رویداد تقویم (فقط پس از تأیید کاربر افزوده می‌شود)
- **تشخیص هوشمند موارد تکراری** با پیاده‌سازی واقعی هش ادراکی (aHash)
- **موتور پاکسازی هوش مصنوعی** — پاکسازی قطعی خطاهای OCR که همیشه متن اصلی را حفظ می‌کند
- **سیستم دفترچه/فضای‌کاری** — پوشه، برچسب، موردعلاقه، سنجاق، بایگانی
- **موتور جستجوی تمام‌متن محلی** (Room FTS4) روی یادداشت‌ها، اسناد و رسیدها
- **استودیوی PDF** (ساخت PDF چندصفحه‌ای از صفحات اسکن‌شده) و خروجی **CSV / JSON / Markdown / TXT**
- **پشتیبان‌گیری و بازیابی محلی رمزنگاری‌شده** — AES/GCM از طریق Android Keystore
- **قفل بیومتریک اپ/دفترچه** با BiometricPrompt
- **مدیریت فضای ذخیره‌سازی** — گزارش واقعی فضای دیسک با گزینه پاک‌سازی امن کش (بدون حذف داده اصلی)
- **مدیریت باتری و کارایی** — تطبیق نرخ نمونه‌برداری OCR زنده و موازی‌سازی کارهای دسته‌ای با سطح باتری/گرمای دستگاه
- **ماژول ساعات کاری** — صفحه‌ای کاملاً قابل ویرایش توسط کاربر (توضیح در ادامه) با وضعیت زنده باز/بسته و شمارش معکوس
- **پنج پوسته**: ویندوز ۱۱ (پیش‌فرض)، روشن، تاریک، قرمز، آبی — قابل تغییر در تنظیمات
- **سه زبان**: انگلیسی (چپ‌به‌راست)، فارسی (راست‌به‌چپ)، چینی (چپ‌به‌راست) با تغییر زبان درون‌برنامه‌ای و مدیریت کامل جهت چیدمان
- **کارهای پس‌زمینه با WorkManager** برای پردازش OCR، اسکن دسته‌ای و پشتیبان‌گیری

### ماژول ساعات کاری

در مسیر **تنظیمات ← ساعات کاری**، می‌توانید یک یا چند برنامه از صفر بسازید: یک عنوان، یک منطقه زمانی IANA (مثلاً `Asia/Tehran`)، و برای هر روز هفته اینکه باز است یا نه و ساعت باز/بسته شدن آن. هیچ‌چیز از پیش پر نشده است. سپس لومن به‌صورت زنده محاسبه می‌کند:

- اینکه **اکنون باز است یا بسته**
- **زمان دقیق باز/بسته شدن بعدی**
- **مدت‌زمان باقی‌مانده** تا آن تغییر (مثلاً «۲ ساعت و ۱۵ دقیقه»)

برنامه‌های شبانه (مثلاً باز ساعت ۱۸:۰۰، بسته ساعت ۰۲:۰۰) نیز پشتیبانی می‌شوند.

### یادداشت صادقانه درباره محدوده پروژه (لطفاً پیش از گزارش ایراد بخوانید)

این یک کدبیس کامل، واقعی و از صفر ساخته‌شده اندروید است (معماری Clean + MVVM، حدود ۱۱۰ فایل Kotlin، حدود ۶۰۰۰ خط) که برای عملکرد واقعی طراحی شده، نه یک نمونه ظاهری؛ هر ویژگی بالا واقعاً به APIهای اندروید/ML Kit/Room/WorkManager/Compose متصل است. با این حال، برای اینکه توانایی کتابخانه‌های زیرین بیش از واقعیت جلوه داده نشود، چند بخش عمداً به‌صورت **ابتکاری و به‌وضوح مشخص‌شده** پیاده‌سازی شده‌اند، نه به‌عنوان مدل یادگیری ماشین آموزش‌دیده:

- **OCR خط فارسی/عربی:** ML Kit در حال حاضر مدل روی‌دستگاه برای خط فارسی/عربی ندارد (فقط لاتین، چینی، ژاپنی، کره‌ای، دیوانگری). متن فارسی ممکن است به‌خوبی تشخیص داده نشود؛ اتصال یک سرویس OCR ابری، نقطه توسعه مستند‌شده است.
- **تشخیص خودکار لبه سند:** یک الگوریتم ابتکاری سبک بر پایه گرادیان روشنایی است، نه یک پایپ‌لاین کامل تشخیص کانتور؛ اصلاح دستی گوشه‌ها همیشه در دسترس است.
- **استخراج جدول:** بر پایه خوشه‌بندی جعبه‌های محدودکننده کار می‌کند و برای جداول شبکه‌ای ساده خوب عمل می‌کند؛ جداول پیچیده نیاز به اصلاح دستی دارند (کاملاً پشتیبانی‌شده در صفحه جزئیات جدول).
- **تشخیص دست‌نویس:** از همان OCR استاندارد به همراه علامت‌گذاری اعتماد پایین استفاده می‌کند؛ مدل اختصاصی HTR همراه نیست.
- **خواندن نمودار/فرمول ریاضی:** جایگزینی نمادی ساده و به‌وضوح «تقریبی» علامت‌گذاری شده؛ نقطه شروع است، نه ویژگی نهایی.
- **خلاصه‌سازی و فلش‌کارت:** NLP استخراجی واقعی روی دستگاه (نه یک مدل زبانی بزرگ)؛ یک رابط اختیاری برای اتصال ارائه‌دهنده هوش مصنوعی ابری وجود دارد.
- **لایه متن قابل‌جستجوی PDF:** فعلاً فقط تصویر صفحات در PDF قرار می‌گیرد، بدون لایه متن نامرئی OCR؛ به‌عنوان نقطه توسعه مستند شده است.

### پیش‌نیازها

- **Android Studio** نسخه Koala (۲۰۲۴.۱) یا جدیدتر
- **JDK 17**
- **Android SDK** پلتفرم ۳۴، Build-Tools ۳۴
- دستگاه فیزیکی یا شبیه‌ساز با **اندروید ۸.۰ (API 26)** یا جدیدتر و دوربین

### نصب و بیلد

۱. پوشه پروژه را در Android Studio باز کنید (`File → Open…`) و اجازه دهید Gradle همگام‌سازی شود.

۲. یا از خط فرمان بیلد بگیرید:

```bash
# از ریشه پروژه
./gradlew assembleDebug
```

اگر فایل جار Gradle Wrapper را ندارید (برای کوچک ماندن حجم آرشیو، `gradle/wrapper/gradle-wrapper.jar` همراه آن نیست)، یک‌بار با یک Gradle نصب‌شده محلی آن را بسازید:

```bash
gradle wrapper --gradle-version 8.7
```

۳. نصب APK ساخته‌شده روی دستگاه/شبیه‌ساز متصل:

```bash
./gradlew installDebug
```

۴. اجرای تست‌های واحد:

```bash
./gradlew test
```

### کتابخانه‌ها و اجزای مورد نیاز

همه موارد زیر در `app/build.gradle.kts` تعریف شده‌اند و به‌صورت خودکار توسط Gradle دریافت می‌شوند (فقط نیاز به اینترنت در اولین بیلد):

```
androidx.core:core-ktx
androidx.appcompat:appcompat
androidx.exifinterface:exifinterface
androidx.compose (BOM) + material3 + navigation-compose
com.google.dagger:hilt-android (+ compiler)
androidx.hilt:hilt-navigation-compose, hilt-work (+ compiler)
androidx.room:room-runtime/ktx (+ compiler)
androidx.work:work-runtime-ktx
androidx.datastore:datastore-preferences
androidx.camera:camera-core/camera2/lifecycle/view
com.google.mlkit:text-recognition, text-recognition-chinese, barcode-scanning
org.tensorflow:tensorflow-lite, tensorflow-lite-support
androidx.biometric:biometric
androidx.security:security-crypto
com.squareup.retrofit2:retrofit (+ kotlinx-serialization converter), okhttp
io.coil-kt:coil-compose
```

### مجوزهای درخواستی

دوربین، میکروفون (اختیاری)، اعلان‌ها، مخاطبین، تقویم و دسترسی شبکه — هرکدام فقط زمانی درخواست می‌شوند که ویژگی مربوطه استفاده شود، و هدف هر مجوز در «مرکز حریم خصوصی» درون‌برنامه توضیح داده شده است.

### مجوز استفاده

این کدبیس اولیه تحت مجوز MIT ارائه می‌شود.

---

## 🇨🇳 中文

### 什么是 LUMEN？

LUMEN 将您手机的摄像头变成一个可视化知识系统。将它对准白板、书页、收据、表格、表单、名片或二维码，LUMEN 会自动识别内容，在设备本地运行文字识别（OCR），并将其转化为结构化、可搜索的内容：**笔记、文档、表格、任务、提醒、收据、闪卡、摘要、引言、联系人**或**日程事件**。

本应用采用**离线优先**架构：拍照捕获、设备端 OCR（Google ML Kit）、本地数据库存储（Room + 全文搜索 FTS）、表格/收据/名片解析、摘要生成、闪卡生成、PDF 导出以及生物识别锁定，均无需联网即可使用。任何云端人工智能或云端翻译服务均为**完全可选**，且只有在"设置"中明确同意后才会启用。

### 本代码库已实现的核心功能

- **智能相机扫描仪** — 真实的 CameraX 流水线（预览、自动对焦、闪光灯、缩放、自适应帧率的实时 OCR 采样）
- **文档处理流水线** — 拍摄 → 预处理 → 检测 → OCR → 结构化 → 清理 → 分析 → 摘要 → 索引 → 保存，每个阶段均有独立进度
- **设备端 OCR**：基于 Google ML Kit（拉丁文与中文模型），并对每行文本进行置信度估算
- **透视校正、去倾斜、对比度增强**，用于文档"展平"
- **表格智能引擎** — 基于 OCR 边界框的行/列启发式聚类，生成可编辑表格（支持导出 CSV、JSON、Markdown）
- **收据与发票解析器** — 基于正则/启发式规则提取商户、日期、总额、税费、货币、支付方式及明细项，每一项均可追溯到其来源的 OCR 行
- **名片解析器** — 提取姓名/公司/职位/电话/邮箱/网站/地址，仅在用户明确确认后才保存到联系人
- **表单字段（键/值）提取器**
- **二维码 / 条形码扫描**（基于 ML Kit）
- **设备端抽取式摘要**（简短 / 中等 / 深入）与**关键词提取**
- **闪卡生成器**：从扫描文本中的问答与定义模式生成
- **日期/截止日期/待办事项提取** → 生成任务、提醒、日历事件（仅在用户确认后添加）
- **智能重复检测**：基于真实的感知哈希（aHash）算法实现
- **AI 清理引擎** — 确定性地清理 OCR 产生的错误，同时始终保留原始文本
- **笔记本/工作区系统** — 文件夹、标签、收藏、置顶、归档
- **本地全文搜索引擎**（Room FTS4），覆盖笔记、文档与收据
- **PDF 制作工坊**（将扫描页面组合成多页 PDF）以及 **CSV / JSON / Markdown / TXT** 导出
- **加密的本地备份与恢复** — 通过 Android Keystore 实现 AES/GCM 加密
- **应用/笔记本生物识别锁定**（基于 BiometricPrompt）
- **存储管理器** — 真实的磁盘空间统计（原图、处理后图像、OCR 数据、PDF、缩略图、缓存），并提供安全的"清除缓存"（绝不删除原始数据）
- **电量与性能管理器** — 根据电量水平与设备发热状态，自适应调整实时 OCR 采样率与批处理并行度
- **营业时间模块** — 一个完全由用户自行编辑的界面（见下文），可实时显示营业/打烊状态及倒计时
- **五种主题**：Windows 11（默认）、浅色、深色、红色、蓝色 — 可在设置中切换
- **三种语言**：英文（从左到右）、波斯语（从右到左）、中文（从左到右），支持应用内语言切换及完整的布局方向处理
- **基于 WorkManager 的后台任务**，用于 OCR 处理、批量扫描与备份

### 营业时间模块

在**设置 → 营业时间**中，您可以从零开始创建一个或多个时间表：一个名称、一个 IANA 时区（例如 `Asia/Shanghai`），以及针对每周每一天设置是否营业及具体的开门/打烊时间。系统不会预填任何内容。LUMEN 会实时计算：

- 当前是**营业中**还是**已打烊**
- 下一次**开门/打烊的准确时间**
- 距离该变化的**剩余时长**（例如"2小时15分钟"）

同时支持跨夜营业时间（例如 18:00 开门，次日 02:00 打烊）。

### 诚实的范围说明（提交问题前请先阅读）

这是一个完整、真实、从零构建的 Android 代码库（Clean Architecture + MVVM，约 110 个 Kotlin 文件，约 6000 行代码），旨在真正可用，而非仅作展示——上述每一项功能都调用了真实的 Android / ML Kit / Room / WorkManager / Compose API，而非占位符。但为了不夸大底层库本身并不具备的自动化能力，有几个方面被有意实现为**明确标注的启发式方法**，而非训练好的机器学习模型，相关代码注释中已明确说明：

- **波斯语/阿拉伯文字 OCR：** Google ML Kit 目前尚未提供设备端阿拉伯文/波斯文识别模型（仅支持拉丁文、中文、日文、韩文、天城文）。波斯语文字的识别效果可能不理想；接入云端 OCR 服务是文档中已标注的扩展点。
- **文档边缘自动检测：** 采用轻量级亮度梯度启发式算法，并非完整的轮廓/霍夫变换流水线；界面始终支持手动拖动角点校正。
- **表格提取：** 基于边界框聚类，对网格状表格效果良好；复杂或合并单元格的表格需要手动修正（表格详情页面完全支持此操作）。
- **手写识别：** 使用标准 OCR 并标记低置信度区域；未内置专门的手写识别（HTR）模型。
- **图表/数学公式识别：** 简单的符号替换，并明确标注为"近似结果"；这是一个起点，而非成品功能。
- **摘要与闪卡：** 真实的设备端抽取式 NLP（非大语言模型）；已提供可选的云端 AI 提供方接口，供有需要的团队接入。
- **"可搜索"PDF 文本层：** 目前仅将扫描页面图像组合为 PDF，尚无隐藏的 OCR 文本层；已作为扩展点在文档中说明。

### 前置条件

- **Android Studio** Koala（2024.1）或更新版本
- **JDK 17**
- **Android SDK** Platform 34，Build-Tools 34
- 运行 **Android 8.0（API 26）**或更高版本、带摄像头的真机或模拟器

### 安装与构建

1. 在 Android Studio 中打开项目文件夹（`File → Open…`），等待 Gradle 同步完成。
2. 或通过命令行构建：

```bash
# 在项目根目录下
./gradlew assembleDebug
```

若没有 Gradle Wrapper 的 jar 文件（为保持压缩包体积较小，本压缩包未包含 `gradle/wrapper/gradle-wrapper.jar`），请使用本地已安装的 Gradle 生成一次：

```bash
gradle wrapper --gradle-version 8.7
```

3. 将生成的 APK 安装到已连接的设备/模拟器：

```bash
./gradlew installDebug
```

4. 运行单元测试：

```bash
./gradlew test
```

### 所需的库 / SDK 组件

以下所有依赖均已在 `app/build.gradle.kts` 中声明，Gradle 会自动获取（首次构建时需要联网）：

```
androidx.core:core-ktx
androidx.appcompat:appcompat
androidx.exifinterface:exifinterface
androidx.compose (BOM) + material3 + navigation-compose
com.google.dagger:hilt-android (+ compiler)
androidx.hilt:hilt-navigation-compose, hilt-work (+ compiler)
androidx.room:room-runtime/ktx (+ compiler)
androidx.work:work-runtime-ktx
androidx.datastore:datastore-preferences
androidx.camera:camera-core/camera2/lifecycle/view
com.google.mlkit:text-recognition, text-recognition-chinese, barcode-scanning
org.tensorflow:tensorflow-lite, tensorflow-lite-support
androidx.biometric:biometric
androidx.security:security-crypto
com.squareup.retrofit2:retrofit (+ kotlinx-serialization converter), okhttp
io.coil-kt:coil-compose
```

### 请求的权限

摄像头、麦克风（可选）、通知、联系人、日历以及网络访问——每项权限仅在使用相应功能时才会请求，且每项权限的用途都会在应用内的"隐私中心"中说明。

### 许可协议

本初始代码库采用 MIT 许可协议提供。

---

<sub>Generated as a real, from-scratch Android project skeleton — not a mockup. See the "Honest scope notes" section above in each language for exactly which parts are heuristic-based versus trained-model-based.</sub>
