# OxygenToolbox

[English](#english) | [简体中文](#简体中文)

---

<h2 id="english">English</h2>

OxygenToolbox is a versatile system utility and tweaking application designed specifically for **OPlus series devices** (OPPO, OnePlus, and Realme) running OxygenOS, ColorOS, or Realme UI. It provides advanced system-level adjustments, diagnostic tools, and hardware info, requiring elevated privileges (**Root**) to function properly.

### 🌟 Features

*   **System Tools:**
    *   **Minus One Screen Options:** Set the left-most screen to OnePlus Shelf, Google Discover, or completely disable it.
    *   **ColorOS SMS:** Activate or silently install the built-in ColorOS SMS application.
*   **System Diagnostics & Experimental Features:**
    *   **ARB (Anti-Rollback) Status Check:** Run diagnostics to check the rollback index and status (Tripped / Not Tripped).
    *   **Anti-ARB (High Risk):** Sync `xbl_config` from the active slot to the inactive slot after an OTA download (before rebooting) to prevent ARB tripping.
    *   **Widevine L1 Fix (High Risk):** Permanently burn the device certification Keybox to the TEE to restore Widevine L1 certification.
*   **Device & Software Information:** View detailed hardware, build info, Android versions, SoC details, and more.
*   **Authorization Management:** Supports Root access (Magisk, KernelSU, APatch) to ensure deep system modifications. *(Note: Shizuku support is available but restricted for modifying core system features.)*
*   **Bilingual Interface:** Automatically adapts to English or Chinese based on your device settings.

### ⚠️ Compatibility Warning
This application is **exclusively designed for OPlus series devices** (OPPO, OnePlus, Realme) and their respective stock ROMs (OxygenOS, ColorOS, Realme UI). 
Using this app on other devices or custom ROMs (such as AOSP, LineageOS) may result in bootloops, soft bricks, or other unpredictable issues.

### 📜 Disclaimer
OxygenToolbox is provided "AS-IS" without any warranties. 
**WE STRONGLY RECOMMEND AGAINST UNLOCKING THE BOOTLOADER AND/OR MODIFYING A DEVICE'S SOFTWARE. DOING SO CAN HAVE UNINTENDED CONSEQUENCES INCLUDING RENDERING THE DEVICE UNUSABLE.** Operate at your own risk.


---

<h2 id="简体中文">简体中文</h2>

OxygenToolbox 是一款专为**欧加（OPlus）系列设备**（OPPO、一加和 Realme）设计的多功能系统辅助工具箱。它针对 OxygenOS / ColorOS / Realme UI 提供了一系列深度的系统级调整、诊断工具和设备信息显示功能。本应用需要提升权限（**Root**）才能完整运行。

### 🌟 功能特点

*   **实用工具：**
    *   **负一屏修改：** 支持将负一屏（左滑）设置为一加 Shelf、Google Discover（探索），或者彻底关闭系统自带的负一屏。
    *   **系统应用：** 激活 ColorOS 短信所需的必要电话状态权限，或静默安装应用内置打包的 ColorOS 短信提取版。
*   **系统诊断与实验性功能：**
    *   **检测 ARB 状态：** 运行防回滚 (Anti-Rollback) 检测工具，获取并判定设备的熔断指数。
    *   **Anti-ARB 防回滚（高风险）：** 将活动槽的 `xbl_config` 同步刷入非活动槽（仅支持在 OTA 下载完成且重启前使用）。
    *   **修复 Widevine L1 认证（高风险）：** 将设备认证 Keybox 永久烧录至 TEE，以恢复 Widevine L1 认证级别。
*   **设备与软件信息：** 详细查看设备型号、硬件配置、支持的 ABI、系统版本及 SoC 信息等。
*   **权限管理：** 深度支持 Root（Magisk / KernelSU / APatch）授权机制。*(注：虽然部分非系统级功能支持 Shizuku 模式，但在“系统”选项卡内会受到严格限制。)*
*   **双语支持：** 界面会自动根据您的系统语言设置在中英双语间无缝切换。

### ⚠️ 兼容性警告
本应用**仅适用于欧加（OPlus）系列设备**（OPPO、OnePlus、Realme）及其官方固件（OxygenOS / ColorOS / Realme UI）。
在其他品牌设备或第三方固件（如 AOSP、LineageOS）上运行本应用，可能会导致不可预知的问题、设备无限重启（Bootloop）或变砖。

### 📜 免责声明
本应用开源并按原样提供，不做任何可用性保证。
**我们强烈建议您不要随意解锁引导加载程序 (Bootloader) 或修改设备的底层软件操作系统。这类操作可能会产生危险及意料之外的后果，例如导致设备永久损坏。** 任何操作由此带来的一切后果均由您自行承担。

