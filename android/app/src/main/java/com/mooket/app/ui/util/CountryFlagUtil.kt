package com.mooket.app.ui.util

/**
 * 国家名称到国旗emoji的映射
 * 使用Unicode国旗emoji
 */
object CountryFlagUtil {

    /**
     * 根据国家名称获取国旗emoji
     * @param countryName 国家名称（中文）
     * @return 国旗emoji，如"🇧🇷"，如果未找到则返回空字符串
     */
    fun getFlagEmoji(countryName: String): String {
        return when (countryName) {
            // 南美国家
            "巴西" -> "\uD83C\uDDE7\uD83C\uDDF7"  // 🇧🇷
            "阿根廷" -> "\uD83C\uDDE6\uD83C\uDDF7"  // 🇦🇷
            "乌拉圭" -> "\uD83C\uDDFA\uD83C\uDDFE"  // 🇺🇾
            "智利" -> "\uD83C\uDDE8\uD83C\uDDF1"  // 🇨🇱
            "秘鲁" -> "\uD83C\uDDF5\uD83C\uDDEA"  // 🇵🇪
            "哥伦比亚" -> "\uD83C\uDDE8\uD83C\uDDF4"  // 🇨🇴
            "巴拉圭" -> "\uD83C\uDDF5\uD83C\uDDFE"  // 🇵🇾
            "玻利维亚" -> "\uD83C\uDDE7\uD83C\uDDF4"  // 🇧🇴
            "委内瑞拉" -> "\uD83C\uDDFB\uD83C\uDDEA"  // 🇻🇪
            "厄瓜多尔" -> "\uD83C\uDDEA\uD83C\uDDE9"  // 🇪🇨

            // 北美国家
            "美国" -> "\uD83C\uDDFA\uD83C\uDDF8"  // 🇺🇸
            "加拿大" -> "\uD83C\uDDE8\uD83C\uDDE6"  // 🇨🇦
            "墨西哥" -> "\uD83C\uDDF2\uD83C\uDDFD"  // 🇲🇽

            // 欧洲国家
            "德国" -> "\uD83C\uDDE9\uD83C\uDDEA"  // 🇩🇪
            "法国" -> "\uD83C\uDDEB\uD83C\uDDF7"  // 🇫🇷
            "英国" -> "\uD83C\uDDEC\uD83C\uDDE7"  // 🇬🇧
            "意大利" -> "\uD83C\uDDEE\uD83C\uDDF9"  // 🇮🇹
            "西班牙" -> "\uD83C\uDDEA\uD83C\uDDF8"  // 🇪🇸
            "荷兰" -> "\uD83C\uDDF3\uD83C\uDDF1"  // 🇳🇱
            "比利时" -> "\uD83C\uDDE7\uD83C\uDDEA"  // 🇧🇪
            "葡萄牙" -> "\uD83C\uDDF5\uD83C\uDDF9"  // 🇵🇹
            "俄罗斯" -> "\uD83C\uDDF7\uD83C\uDDFA"  // 🇷🇺
            "乌克兰" -> "\uD83C\uDDFA\uD83C\uDDE6"  // 🇺🇦
            "波兰" -> "\uD83C\uDDF5\uD83C\uDDF1"  // 🇵🇱

            // 亚洲国家
            "中国" -> "\uD83C\uDDE8\uD83C\uDDF3"  // 🇨🇳
            "日本" -> "\uD83C\uDDEF\uD83C\uDDF5"  // 🇯🇵
            "韩国" -> "\uD83C\uDDF0\uD83C\uDDF7"  // 🇰🇷
            "泰国" -> "\uD83C\uDDF9\uD83C\uDDF4"  // 🇹🇭
            "越南" -> "\uD83C\uDDFB\uD83C\uDDF3"  // 🇻🇳
            "印度" -> "\uD83C\uDDEE\uD83C\uDDF3"  // 🇮🇳
            "印度尼西亚" -> "\uD83C\uDDEE\uD83C\uDDE9"  // 🇮🇩 (incomplete, use first char)
            "马来西亚" -> "\uD83C\uDDF2\uD83C\uDDFE"  // 🇲🇾
            "新加坡" -> "\uD83C\uDDF8\uD83C\uDDEC"  // 🇸🇬
            "菲律宾" -> "\uD83C\uDDF5\uD83C\uDDED"  // 🇵🇭
            "巴基斯坦" -> "\uD83C\uDDF5\uD83C\uDDF0"  // 🇵🇰
            "哈萨克斯坦" -> "\uD83C\uDDF0\uD83C\uDDFF"  // 🇰🇿
            "蒙古" -> "\uD83C\uDDF2\uD83C\uDDF3"  // 🇲🇳

            // 大洋洲国家
            "澳大利亚" -> "\uD83C\uDDE6\uD83C\uDDFA"  // 🇦🇺
            "新西兰" -> "\uD83C\uDDF3\uD83C\uDDFF"  // 🇳🇿

            // 非洲国家
            "南非" -> "\uD83C\uDDFF\uD83C\uDDE6"  // 🇿🇦
            "埃及" -> "\uD83C\uDDEA\uD83C\uDDEC"  // 🇪🇬
            "尼日利亚" -> "\uD83C\uDDF3\uD83C\uDDEC"  // 🇳🇬
            "肯尼亚" -> "\uD83C\uDDF0\uD83C\uDDEA"  // 🇰🇪
            "摩洛哥" -> "\uD83C\uDDF2\uD83C\uDDE6"  // 🇲🇦
            "埃塞俄比亚" -> "\uD83C\uDDEA\uD83C\uDDF9"  // 🇪🇹

            // 未知/默认
            else -> ""
        }
    }

    /**
     * 根据国家代码获取国旗emoji（适用于英文或代码）
     */
    fun getFlagEmojiByCode(countryCode: String): String {
        if (countryCode.length != 2) return ""
        val code = countryCode.uppercase()
        // 将国家代码转换为区域指示符 emoji
        // A-Z 的字母对应 U+1F1E6 到 U+1F1FF
        val firstChar = code[0] - 'A' + 0x1F1E6
        val secondChar = code[1] - 'A' + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}
