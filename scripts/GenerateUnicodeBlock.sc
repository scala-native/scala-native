// Run with $ amm scripts/GenerateUnicodeBlock.sc
//   Replace code in ./javalanglib/src/main/scala/java/lang/Character.scala

import java.net.URL
import java.nio.charset.StandardCharsets

val blocksSourceURL: URL = new URL(
  "http://unicode.org/Public/UCD/latest/ucd/Blocks.txt")

// Generated/created via Mapping Worksheet:
//   https://docs.google.com/spreadsheets/d/174aedhJlWNZYEEJtSVcIiMFvD7f27rg4NssjFw0hIDw/edit?usp=sharing

val ucdBlockNameToJDKBlockName: Map[String, String] = Map(
  "Adlam"                                          -> "ADLAM",
  "Aegean Numbers"                                 -> "AEGEAN_NUMBERS",
  "Ahom"                                           -> "AHOM",
  "Alchemical Symbols"                             -> "ALCHEMICAL_SYMBOLS",
  "Alphabetic Presentation Forms"                  -> "ALPHABETIC_PRESENTATION_FORMS",
  "Alchemical Symbols"                             -> "ANATOLIAN_HIEROGLYPHS",
  "Ancient Greek Musical Notation"                 -> "ANCIENT_GREEK_MUSICAL_NOTATION",
  "Ancient Greek Numbers"                          -> "ANCIENT_GREEK_NUMBERS",
  "Ancient Symbols"                                -> "ANCIENT_SYMBOLS",
  "Arabic"                                         -> "ARABIC",
  "Arabic Extended-A"                              -> "ARABIC_EXTENDED_A",
  "Arabic Mathematical Alphabetic Symbols"         -> "ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS",
  "Arabic Presentation Forms-A"                    -> "ARABIC_PRESENTATION_FORMS_A",
  "Arabic Presentation Forms-B"                    -> "ARABIC_PRESENTATION_FORMS_B",
  "Arabic Supplement"                              -> "ARABIC_SUPPLEMENT",
  "Armenian"                                       -> "ARMENIAN",
  "Arrows"                                         -> "ARROWS",
  "Avestan"                                        -> "AVESTAN",
  "Balinese"                                       -> "BALINESE",
  "Bamum"                                          -> "BAMUM",
  "Bamum Supplement"                               -> "BAMUM_SUPPLEMENT",
  "Basic Latin"                                    -> "BASIC_LATIN",
  "Bassa Vah"                                      -> "BASSA_VAH",
  "Batak"                                          -> "BATAK",
  "Bengali"                                        -> "BENGALI",
  "Bhaiksuki"                                      -> "BHAIKSUKI",
  "Block Elements"                                 -> "BLOCK_ELEMENTS",
  "Bopomofo"                                       -> "BOPOMOFO",
  "Bopomofo Extended"                              -> "BOPOMOFO_EXTENDED",
  "Box Drawing"                                    -> "BOX_DRAWING",
  "Brahmi"                                         -> "BRAHMI",
  "Braille Patterns"                               -> "BRAILLE_PATTERNS",
  "Buginese"                                       -> "BUGINESE",
  "Buhid"                                          -> "BUHID",
  "Byzantine Musical Symbols"                      -> "BYZANTINE_MUSICAL_SYMBOLS",
  "Carian"                                         -> "CARIAN",
  "Caucasian Albanian"                             -> "CAUCASIAN_ALBANIAN",
  "Chakma"                                         -> "CHAKMA",
  "Cham"                                           -> "CHAM",
  "Cherokee"                                       -> "CHEROKEE",
  "Cherokee Supplement"                            -> "CHEROKEE_SUPPLEMENT",
  "Chess Symbols"                                  -> "CHESS_SYMBOLS",
  "CJK Compatibility"                              -> "CJK_COMPATIBILITY",
  "CJK Compatibility Forms"                        -> "CJK_COMPATIBILITY_FORMS",
  "CJK Compatibility Ideographs"                   -> "CJK_COMPATIBILITY_IDEOGRAPHS",
  "CJK Compatibility Ideographs Supplement"        -> "CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT",
  "CJK Radicals Supplement"                        -> "CJK_RADICALS_SUPPLEMENT",
  "CJK Strokes"                                    -> "CJK_STROKES",
  "CJK Symbols and Punctuation"                    -> "CJK_SYMBOLS_AND_PUNCTUATION",
  "CJK Unified Ideographs"                         -> "CJK_UNIFIED_IDEOGRAPHS",
  "CJK Unified Ideographs Extension A"             -> "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A",
  "CJK Unified Ideographs Extension B"             -> "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B",
  "CJK Unified Ideographs Extension C"             -> "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C",
  "CJK Unified Ideographs Extension D"             -> "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D",
  "CJK Unified Ideographs Extension E"             -> "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E",
  "CJK Unified Ideographs Extension F"             -> "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F",
  "Combining Diacritical Marks"                    -> "COMBINING_DIACRITICAL_MARKS",
  "Combining Diacritical Marks Extended"           -> "COMBINING_DIACRITICAL_MARKS_EXTENDED",
  "Combining Diacritical Marks Supplement"         -> "COMBINING_DIACRITICAL_MARKS_SUPPLEMENT",
  "Combining Half Marks"                           -> "COMBINING_HALF_MARKS",
  "Combining Diacritical Marks for Symbols"        -> "COMBINING_MARKS_FOR_SYMBOLS",
  "Common Indic Number Forms"                      -> "COMMON_INDIC_NUMBER_FORMS",
  "Control Pictures"                               -> "CONTROL_PICTURES",
  "Coptic"                                         -> "COPTIC",
  "Coptic Epact Numbers"                           -> "COPTIC_EPACT_NUMBERS",
  "Counting Rod Numerals"                          -> "COUNTING_ROD_NUMERALS",
  "Cuneiform"                                      -> "CUNEIFORM",
  "Cuneiform Numbers and Punctuation"              -> "CUNEIFORM_NUMBERS_AND_PUNCTUATION",
  "Currency Symbols"                               -> "CURRENCY_SYMBOLS",
  "Cypriot Syllabary"                              -> "CYPRIOT_SYLLABARY",
  "Cyrillic"                                       -> "CYRILLIC",
  "Cyrillic Extended-A"                            -> "CYRILLIC_EXTENDED_A",
  "Cyrillic Extended-B"                            -> "CYRILLIC_EXTENDED_B",
  "Cyrillic Extended-C"                            -> "CYRILLIC_EXTENDED_C",
  "Cyrillic Supplement"                            -> "CYRILLIC_SUPPLEMENTARY",
  "Deseret"                                        -> "DESERET",
  "Devanagari"                                     -> "DEVANAGARI",
  "Devanagari Extended"                            -> "DEVANAGARI_EXTENDED",
  "Dingbats"                                       -> "DINGBATS",
  "Dogra"                                          -> "DOGRA",
  "Domino Tiles"                                   -> "DOMINO_TILES",
  "Duployan"                                       -> "DUPLOYAN",
  "Early Dynastic Cuneiform"                       -> "EARLY_DYNASTIC_CUNEIFORM",
  "Egyptian Hieroglyph Format Controls"            -> "EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS",
  "Egyptian Hieroglyphs"                           -> "EGYPTIAN_HIEROGLYPHS",
  "Elbasan"                                        -> "ELBASAN",
  "Elymaic"                                        -> "ELYMAIC",
  "Emoticons"                                      -> "EMOTICONS",
  "Enclosed Alphanumeric Supplement"               -> "ENCLOSED_ALPHANUMERIC_SUPPLEMENT",
  "Enclosed Alphanumerics"                         -> "ENCLOSED_ALPHANUMERICS",
  "Enclosed CJK Letters and Months"                -> "ENCLOSED_CJK_LETTERS_AND_MONTHS",
  "Enclosed Ideographic Supplement"                -> "ENCLOSED_IDEOGRAPHIC_SUPPLEMENT",
  "Ethiopic"                                       -> "ETHIOPIC",
  "Ethiopic Extended"                              -> "ETHIOPIC_EXTENDED",
  "Ethiopic Extended-A"                            -> "ETHIOPIC_EXTENDED_A",
  "Ethiopic Supplement"                            -> "ETHIOPIC_SUPPLEMENT",
  "General Punctuation"                            -> "GENERAL_PUNCTUATION",
  "Geometric Shapes"                               -> "GEOMETRIC_SHAPES",
  "Geometric Shapes Extended"                      -> "GEOMETRIC_SHAPES_EXTENDED",
  "Georgian"                                       -> "GEORGIAN",
  "Georgian Extended"                              -> "GEORGIAN_EXTENDED",
  "Georgian Supplement"                            -> "GEORGIAN_SUPPLEMENT",
  "Glagolitic"                                     -> "GLAGOLITIC",
  "Glagolitic Supplement"                          -> "GLAGOLITIC_SUPPLEMENT",
  "Gothic"                                         -> "GOTHIC",
  "Grantha"                                        -> "GRANTHA",
  "Greek and Coptic"                               -> "GREEK",
  "Greek Extended"                                 -> "GREEK_EXTENDED",
  "Gujarati"                                       -> "GUJARATI",
  "Gunjala Gondi"                                  -> "GUNJALA_GONDI",
  "Gurmukhi"                                       -> "GURMUKHI",
  "Halfwidth and Fullwidth Forms"                  -> "HALFWIDTH_AND_FULLWIDTH_FORMS",
  "Hangul Compatibility Jamo"                      -> "HANGUL_COMPATIBILITY_JAMO",
  "Hangul Jamo"                                    -> "HANGUL_JAMO",
  "Hangul Jamo Extended-A"                         -> "HANGUL_JAMO_EXTENDED_A",
  "Hangul Jamo Extended-B"                         -> "HANGUL_JAMO_EXTENDED_B",
  "Hangul Syllables"                               -> "HANGUL_SYLLABLES",
  "Hanifi Rohingya"                                -> "HANIFI_ROHINGYA",
  "Hanunoo"                                        -> "HANUNOO",
  "Hatran"                                         -> "HATRAN",
  "Hebrew"                                         -> "HEBREW",
  "High Private Use Surrogates"                    -> "HIGH_PRIVATE_USE_SURROGATES",
  "High Surrogates"                                -> "HIGH_SURROGATES",
  "Hiragana"                                       -> "HIRAGANA",
  "Ideographic Description Characters"             -> "IDEOGRAPHIC_DESCRIPTION_CHARACTERS",
  "Ideographic Symbols And Punctuation"            -> "IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION",
  "Imperial Aramaic"                               -> "IMPERIAL_ARAMAIC",
  "Indic Siyaq Numbers"                            -> "INDIC_SIYAQ_NUMBERS",
  "Inscriptional Pahlavi"                          -> "INSCRIPTIONAL_PAHLAVI",
  "Inscriptional Parthian"                         -> "INSCRIPTIONAL_PARTHIAN",
  "IPA Extensions"                                 -> "IPA_EXTENSIONS",
  "Javanese"                                       -> "JAVANESE",
  "Kaithi"                                         -> "KAITHI",
  "Kana Extended-A"                                -> "KANA_EXTENDED_A",
  "Kana Supplement"                                -> "KANA_SUPPLEMENT",
  "Kanbun"                                         -> "KANBUN",
  "Kangxi Radicals"                                -> "KANGXI_RADICALS",
  "Kannada"                                        -> "KANNADA",
  "Katakana"                                       -> "KATAKANA",
  "Katakana Phonetic Extensions"                   -> "KATAKANA_PHONETIC_EXTENSIONS",
  "Kayah Li"                                       -> "KAYAH_LI",
  "Kharoshthi"                                     -> "KHAROSHTHI",
  "Khmer"                                          -> "KHMER",
  "Khmer Symbols"                                  -> "KHMER_SYMBOLS",
  "Khojki"                                         -> "KHOJKI",
  "Khudawadi"                                      -> "KHUDAWADI",
  "Lao"                                            -> "LAO",
  "Latin-1 Supplement"                             -> "LATIN_1_SUPPLEMENT",
  "Latin Extended-A"                               -> "LATIN_EXTENDED_A",
  "Latin Extended Additional"                      -> "LATIN_EXTENDED_ADDITIONAL",
  "Latin Extended-B"                               -> "LATIN_EXTENDED_B",
  "Latin Extended-C"                               -> "LATIN_EXTENDED_C",
  "Latin Extended-D"                               -> "LATIN_EXTENDED_D",
  "Latin Extended-E"                               -> "LATIN_EXTENDED_E",
  "Lepcha"                                         -> "LEPCHA",
  "Letterlike Symbols"                             -> "LETTERLIKE_SYMBOLS",
  "Limbu"                                          -> "LIMBU",
  "Linear A"                                       -> "LINEAR_A",
  "Linear B Ideograms"                             -> "LINEAR_B_IDEOGRAMS",
  "Linear B Syllabary"                             -> "LINEAR_B_SYLLABARY",
  "Lisu"                                           -> "LISU",
  "Low Surrogates"                                 -> "LOW_SURROGATES",
  "Lycian"                                         -> "LYCIAN",
  "Lydian"                                         -> "LYDIAN",
  "Mahajani"                                       -> "MAHAJANI",
  "Mahjong Tiles"                                  -> "MAHJONG_TILES",
  "Makasar"                                        -> "MAKASAR",
  "Malayalam"                                      -> "MALAYALAM",
  "Mandaic"                                        -> "MANDAIC",
  "Manichaean"                                     -> "MANICHAEAN",
  "Marchen"                                        -> "MARCHEN",
  "Masaram Gondi"                                  -> "MASARAM_GONDI",
  "Mathematical Alphanumeric Symbols"              -> "MATHEMATICAL_ALPHANUMERIC_SYMBOLS",
  "Mathematical Operators"                         -> "MATHEMATICAL_OPERATORS",
  "Mayan Numerals"                                 -> "MAYAN_NUMERALS",
  "Medefaidrin"                                    -> "MEDEFAIDRIN",
  "Meetei Mayek"                                   -> "MEETEI_MAYEK",
  "Meetei Mayek Extensions"                        -> "MEETEI_MAYEK_EXTENSIONS",
  "Mende Kikakui"                                  -> "MENDE_KIKAKUI",
  "Meroitic Cursive"                               -> "MEROITIC_CURSIVE",
  "Meroitic Hieroglyphs"                           -> "MEROITIC_HIEROGLYPHS",
  "Miao"                                           -> "MIAO",
  "Miscellaneous Mathematical Symbols-A"           -> "MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A",
  "Miscellaneous Mathematical Symbols-B"           -> "MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B",
  "Miscellaneous Symbols"                          -> "MISCELLANEOUS_SYMBOLS",
  "Miscellaneous Symbols and Arrows"               -> "MISCELLANEOUS_SYMBOLS_AND_ARROWS",
  "Miscellaneous Symbols And Pictographs"          -> "MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS",
  "Miscellaneous Technical"                        -> "MISCELLANEOUS_TECHNICAL",
  "Modi"                                           -> "MODI",
  "Modifier Tone Letters"                          -> "MODIFIER_TONE_LETTERS",
  "Mongolian"                                      -> "MONGOLIAN",
  "Mongolian Supplement"                           -> "MONGOLIAN_SUPPLEMENT",
  "Mro"                                            -> "MRO",
  "Multani"                                        -> "MULTANI",
  "Musical Symbols"                                -> "MUSICAL_SYMBOLS",
  "Myanmar"                                        -> "MYANMAR",
  "Myanmar Extended-A"                             -> "MYANMAR_EXTENDED_A",
  "Myanmar Extended-B"                             -> "MYANMAR_EXTENDED_B",
  "Nabataean"                                      -> "NABATAEAN",
  "Nandinagari"                                    -> "NANDINAGARI",
  "New Tai Lue"                                    -> "NEW_TAI_LUE",
  "Newa"                                           -> "NEWA",
  "Nko"                                            -> "NKO",
  "Number Forms"                                   -> "NUMBER_FORMS",
  "Nushu"                                          -> "NUSHU",
  "Nyiakeng Puachue Hmong"                         -> "NYIAKENG_PUACHUE_HMONG",
  "Ogham"                                          -> "OGHAM",
  "Ol Chiki"                                       -> "OL_CHIKI",
  "Old Hungarian"                                  -> "OLD_HUNGARIAN",
  "Old Italic"                                     -> "OLD_ITALIC",
  "Old North Arabian"                              -> "OLD_NORTH_ARABIAN",
  "Old Permic"                                     -> "OLD_PERMIC",
  "Old Persian"                                    -> "OLD_PERSIAN",
  "Old Sogdian"                                    -> "OLD_SOGDIAN",
  "Old South Arabian"                              -> "OLD_SOUTH_ARABIAN",
  "Old Turkic"                                     -> "OLD_TURKIC",
  "Optical Character Recognition"                  -> "OPTICAL_CHARACTER_RECOGNITION",
  "Oriya"                                          -> "ORIYA",
  "Ornamental Dingbats"                            -> "ORNAMENTAL_DINGBATS",
  "Osage"                                          -> "OSAGE",
  "Osmanya"                                        -> "OSMANYA",
  "Ottoman Siyaq Numbers"                          -> "OTTOMAN_SIYAQ_NUMBERS",
  "Pahawh Hmong"                                   -> "PAHAWH_HMONG",
  "Palmyrene"                                      -> "PALMYRENE",
  "Pau Cin Hau"                                    -> "PAU_CIN_HAU",
  "Phags-pa"                                       -> "PHAGS_PA",
  "Phaistos Disc"                                  -> "PHAISTOS_DISC",
  "Phoenician"                                     -> "PHOENICIAN",
  "Phonetic Extensions"                            -> "PHONETIC_EXTENSIONS",
  "Phonetic Extensions Supplement"                 -> "PHONETIC_EXTENSIONS_SUPPLEMENT",
  "Playing Cards"                                  -> "PLAYING_CARDS",
  "Private Use Area"                               -> "PRIVATE_USE_AREA",
  "Psalter Pahlavi"                                -> "PSALTER_PAHLAVI",
  "Rejang"                                         -> "REJANG",
  "Rumi Numeral Symbols"                           -> "RUMI_NUMERAL_SYMBOLS",
  "Runic"                                          -> "RUNIC",
  "Samaritan"                                      -> "SAMARITAN",
  "Saurashtra"                                     -> "SAURASHTRA",
  "Sharada"                                        -> "SHARADA",
  "Shavian"                                        -> "SHAVIAN",
  "Shorthand Format Controls"                      -> "SHORTHAND_FORMAT_CONTROLS",
  "Siddham"                                        -> "SIDDHAM",
  "Sinhala"                                        -> "SINHALA",
  "Sinhala Archaic Numbers"                        -> "SINHALA_ARCHAIC_NUMBERS",
  "Small Form Variants"                            -> "SMALL_FORM_VARIANTS",
  "Small Kana Extension"                           -> "SMALL_KANA_EXTENSION",
  "Sogdian"                                        -> "SOGDIAN",
  "Sora Sompeng"                                   -> "SORA_SOMPENG",
  "Soyombo"                                        -> "SOYOMBO",
  "Spacing Modifier Letters"                       -> "SPACING_MODIFIER_LETTERS",
  "Specials"                                       -> "SPECIALS",
  "Sundanese"                                      -> "SUNDANESE",
  "Sundanese Supplement"                           -> "SUNDANESE_SUPPLEMENT",
  "Superscripts and Subscripts"                    -> "SUPERSCRIPTS_AND_SUBSCRIPTS",
  "Supplemental Arrows-A"                          -> "SUPPLEMENTAL_ARROWS_A",
  "Supplemental Arrows-B"                          -> "SUPPLEMENTAL_ARROWS_B",
  "Supplemental Arrows-C"                          -> "SUPPLEMENTAL_ARROWS_C",
  "Supplemental Mathematical Operators"            -> "SUPPLEMENTAL_MATHEMATICAL_OPERATORS",
  "Supplemental Punctuation"                       -> "SUPPLEMENTAL_PUNCTUATION",
  "Supplemental Symbols And Pictographs"           -> "SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS",
  "Supplementary Private Use Area-A"               -> "SUPPLEMENTARY_PRIVATE_USE_AREA_A",
  "Supplementary Private Use Area-B"               -> "SUPPLEMENTARY_PRIVATE_USE_AREA_B",
  "Sutton Signwriting"                             -> "SUTTON_SIGNWRITING",
  "Syloti Nagri"                                   -> "SYLOTI_NAGRI",
  "Symbols And Pictographs Extended-A"             -> "SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A",
  "Syriac"                                         -> "SYRIAC",
  "Syriac Supplement"                              -> "SYRIAC_SUPPLEMENT",
  "Tagalog"                                        -> "TAGALOG",
  "Tagbanwa"                                       -> "TAGBANWA",
  "Tags"                                           -> "TAGS",
  "Tai Le"                                         -> "TAI_LE",
  "Tai Tham"                                       -> "TAI_THAM",
  "Tai Viet"                                       -> "TAI_VIET",
  "Tai Xuan Jing Symbols"                          -> "TAI_XUAN_JING_SYMBOLS",
  "Takri"                                          -> "TAKRI",
  "Tamil"                                          -> "TAMIL",
  "Tamil Supplement"                               -> "TAMIL_SUPPLEMENT",
  "Tangut"                                         -> "TANGUT",
  "Tangut Components"                              -> "TANGUT_COMPONENTS",
  "Telugu"                                         -> "TELUGU",
  "Thaana"                                         -> "THAANA",
  "Thai"                                           -> "THAI",
  "Tibetan"                                        -> "TIBETAN",
  "Tifinagh"                                       -> "TIFINAGH",
  "Tirhuta"                                        -> "TIRHUTA",
  "Transport And Map Symbols"                      -> "TRANSPORT_AND_MAP_SYMBOLS",
  "Ugaritic"                                       -> "UGARITIC",
  "Unified Canadian Aboriginal Syllabics"          -> "UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS",
  "Unified Canadian Aboriginal Syllabics Extended" -> "UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED",
  "Vai"                                            -> "VAI",
  "Variation Selectors"                            -> "VARIATION_SELECTORS",
  "Variation Selectors Supplement"                 -> "VARIATION_SELECTORS_SUPPLEMENT",
  "Vedic Extensions"                               -> "VEDIC_EXTENSIONS",
  "Vertical Forms"                                 -> "VERTICAL_FORMS",
  "Wancho"                                         -> "WANCHO",
  "Warang Citi"                                    -> "WARANG_CITI",
  "Yi Radicals"                                    -> "YI_RADICALS",
  "Yi Syllables"                                   -> "YI_SYLLABLES",
  "Yijing Hexagram Symbols"                        -> "YIJING_HEXAGRAM_SYMBOLS",
  "Zanabazar Square"                               -> "ZANABAZAR_SQUARE"
)

val source =
  scala.io.Source.fromURL(blocksSourceURL, StandardCharsets.UTF_8.toString)

/* File Example:
 *
 * # @missing: 0000..10FFFF; No_Block
 *
 * 0000..007F; Basic Latin
 * 0080..00FF; Latin-1 Supplement
 * 0100..017F; Latin Extended-A
 */

final case class UnicodeBlock(name: String,
                              codePointStart: String,
                              codePointEnd: String)

val blocks: Seq[UnicodeBlock] = {
  source
    .getLines()
    .filterNot {
      _.startsWith("#")
    }
    .flatMap { line =>
      line.split(';') match {
        case Array(points, name) =>
          val Array(start, end) = points.split(raw"\.\.")
          Some(
            UnicodeBlock(
              name = name.trim,
              codePointStart = start.trim,
              codePointEnd = end.trim
            ))
        case _ => None
      }
    }
    .toIndexedSeq
}

println({
  val jdkConstantsDefinitions: Seq[String] = {
    for {
      block: UnicodeBlock <- blocks
      jdkName: String     <- ucdBlockNameToJDKBlockName.get(block.name)
    } yield s"""    val ${jdkName} = new UnicodeBlock("$jdkName", 0x${block.codePointStart}, 0x${block.codePointEnd})"""
  }

  val jdkConstantVariableNames: Seq[String] = {
    for {
      block: UnicodeBlock <- blocks
      jdkName: String     <- ucdBlockNameToJDKBlockName.get(block.name)
    } yield s"""      $jdkName"""
  }

  // Make sure this is in-sync with private def in generated code
  def toNormalizedName(name: String): String = {
    name.toLowerCase.replaceAll(raw"[\s\-_]", "")
  }

  val normalizedNameToJDKContstant: Seq[String] = {
    for {
      block: UnicodeBlock <- blocks
      jdkName: String     <- ucdBlockNameToJDKBlockName.get(block.name)
    } yield s"""      "${toNormalizedName(block.name)}" -> $jdkName"""
  }

  println(jdkConstantsDefinitions)
  s"""|
      |    //////////////////
      |    // Begin Generated
      |    //////////////////
      |
      |    // scalastyle:off line.size.limit
      |    val SURROGATES_AREA = new UnicodeBlock("SURROGATES_AREA", 0x0, 0x0)
      |${jdkConstantsDefinitions.mkString("\n")}
      |    // scalastyle:on line.size.limit
      |
      |    private val allBlocks: Array[UnicodeBlock] = Array(
      |${jdkConstantVariableNames.mkString(",\n")}
      |    )
      |
      |    import scala.Predef.ArrowAssoc
      |    private val blocksByNormalizedName: scala.collection.Map[String,UnicodeBlock] = scala.collection.Map(
      |${normalizedNameToJDKContstant.mkString(",\n")}
      |    )
      |
      |    // From: https://unicode.org/Public/UCD/latest/ucd/Blocks.txt
      |    // Note:   When comparing block names, casing, whitespace, hyphens,
      |    //         and underbars are ignored.
      |    //         For example, "Latin Extended-A" and "latin extended a" are equivalent.
      |    //         For more information on the comparison of property values,
      |    //           see UAX #44: http://www.unicode.org/reports/tr44/
      |    private def toNormalizedName(name: String): String = {
      |      name.toLowerCase.replaceAll(raw"[\\s\\-_]", "")
      |    }
      |
      |    ////////////////
      |    // End Generated
      |    ////////////////
      |""".stripMargin
})
