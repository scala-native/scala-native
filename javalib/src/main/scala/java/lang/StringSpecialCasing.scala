package java.lang

object StringSpecialCasing {
  /* Generated based on unconditional mappings in [SpecialCasing.txt](https://unicode.org/Public/13.0.0/ucd/SpecialCasing.txt)
   *
   * Relevant excerpt from SpecialCasing.txt
   * # The German es-zed is special--the normal mapping is to SS.
   * # Note: the titlecase should never occur in practice. It is equal to titlecase(uppercase(<es-zed>))
   *
   * 00DF; 00DF; 0053 0073; 0053 0053; # LATIN SMALL LETTER SHARP S
   *
   * # Preserve canonical equivalence for I with dot. Turkic is handled below.
   *
   * 0130; 0069 0307; 0130; 0130; # LATIN CAPITAL LETTER I WITH DOT ABOVE
   *
   * # Ligatures
   *
   * FB00; FB00; 0046 0066; 0046 0046; # LATIN SMALL LIGATURE FF
   * FB01; FB01; 0046 0069; 0046 0049; # LATIN SMALL LIGATURE FI
   * FB02; FB02; 0046 006C; 0046 004C; # LATIN SMALL LIGATURE FL
   * FB03; FB03; 0046 0066 0069; 0046 0046 0049; # LATIN SMALL LIGATURE FFI
   * FB04; FB04; 0046 0066 006C; 0046 0046 004C; # LATIN SMALL LIGATURE FFL
   * FB05; FB05; 0053 0074; 0053 0054; # LATIN SMALL LIGATURE LONG S T
   * FB06; FB06; 0053 0074; 0053 0054; # LATIN SMALL LIGATURE ST
   *
   * 0587; 0587; 0535 0582; 0535 0552; # ARMENIAN SMALL LIGATURE ECH YIWN
   * FB13; FB13; 0544 0576; 0544 0546; # ARMENIAN SMALL LIGATURE MEN NOW
   * FB14; FB14; 0544 0565; 0544 0535; # ARMENIAN SMALL LIGATURE MEN ECH
   * FB15; FB15; 0544 056B; 0544 053B; # ARMENIAN SMALL LIGATURE MEN INI
   * FB16; FB16; 054E 0576; 054E 0546; # ARMENIAN SMALL LIGATURE VEW NOW
   * FB17; FB17; 0544 056D; 0544 053D; # ARMENIAN SMALL LIGATURE MEN XEH
   *
   * # No corresponding uppercase precomposed character
   *
   * 0149; 0149; 02BC 004E; 02BC 004E; # LATIN SMALL LETTER N PRECEDED BY APOSTROPHE
   * 0390; 0390; 0399 0308 0301; 0399 0308 0301; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
   * 03B0; 03B0; 03A5 0308 0301; 03A5 0308 0301; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
   * 01F0; 01F0; 004A 030C; 004A 030C; # LATIN SMALL LETTER J WITH CARON
   * 1E96; 1E96; 0048 0331; 0048 0331; # LATIN SMALL LETTER H WITH LINE BELOW
   * 1E97; 1E97; 0054 0308; 0054 0308; # LATIN SMALL LETTER T WITH DIAERESIS
   * 1E98; 1E98; 0057 030A; 0057 030A; # LATIN SMALL LETTER W WITH RING ABOVE
   * 1E99; 1E99; 0059 030A; 0059 030A; # LATIN SMALL LETTER Y WITH RING ABOVE
   * 1E9A; 1E9A; 0041 02BE; 0041 02BE; # LATIN SMALL LETTER A WITH RIGHT HALF RING
   * 1F50; 1F50; 03A5 0313; 03A5 0313; # GREEK SMALL LETTER UPSILON WITH PSILI
   * 1F52; 1F52; 03A5 0313 0300; 03A5 0313 0300; # GREEK SMALL LETTER UPSILON WITH PSILI AND VARIA
   * 1F54; 1F54; 03A5 0313 0301; 03A5 0313 0301; # GREEK SMALL LETTER UPSILON WITH PSILI AND OXIA
   * 1F56; 1F56; 03A5 0313 0342; 03A5 0313 0342; # GREEK SMALL LETTER UPSILON WITH PSILI AND PERISPOMENI
   * 1FB6; 1FB6; 0391 0342; 0391 0342; # GREEK SMALL LETTER ALPHA WITH PERISPOMENI
   * 1FC6; 1FC6; 0397 0342; 0397 0342; # GREEK SMALL LETTER ETA WITH PERISPOMENI
   * 1FD2; 1FD2; 0399 0308 0300; 0399 0308 0300; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND VARIA
   * 1FD3; 1FD3; 0399 0308 0301; 0399 0308 0301; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND OXIA
   * 1FD6; 1FD6; 0399 0342; 0399 0342; # GREEK SMALL LETTER IOTA WITH PERISPOMENI
   * 1FD7; 1FD7; 0399 0308 0342; 0399 0308 0342; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND PERISPOMENI
   * 1FE2; 1FE2; 03A5 0308 0300; 03A5 0308 0300; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND VARIA
   * 1FE3; 1FE3; 03A5 0308 0301; 03A5 0308 0301; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND OXIA
   * 1FE4; 1FE4; 03A1 0313; 03A1 0313; # GREEK SMALL LETTER RHO WITH PSILI
   * 1FE6; 1FE6; 03A5 0342; 03A5 0342; # GREEK SMALL LETTER UPSILON WITH PERISPOMENI
   * 1FE7; 1FE7; 03A5 0308 0342; 03A5 0308 0342; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND PERISPOMENI
   * 1FF6; 1FF6; 03A9 0342; 03A9 0342; # GREEK SMALL LETTER OMEGA WITH PERISPOMENI
   *
   * # IMPORTANT-when iota-subscript (0345) is uppercased or titlecased,
   * #  the result will be incorrect unless the iota-subscript is moved to the end
   * #  of any sequence of combining marks. Otherwise, the accents will go on the capital iota.
   * #  This process can be achieved by first transforming the text to NFC before casing.
   * #  E.g. <alpha><iota_subscript><acute> is uppercased to <ALPHA><acute><IOTA>
   *
   * # The following cases are already in the UnicodeData.txt file, so are only commented here.
   *
   * # 0345; 0345; 0399; 0399; # COMBINING GREEK YPOGEGRAMMENI
   *
   * # All letters with YPOGEGRAMMENI (iota-subscript) or PROSGEGRAMMENI (iota adscript)
   * # have special uppercases.
   * # Note: characters with PROSGEGRAMMENI are actually titlecase, not uppercase!
   *
   * 1F80; 1F80; 1F88; 1F08 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND YPOGEGRAMMENI
   * 1F81; 1F81; 1F89; 1F09 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND YPOGEGRAMMENI
   * 1F82; 1F82; 1F8A; 1F0A 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND VARIA AND YPOGEGRAMMENI
   * 1F83; 1F83; 1F8B; 1F0B 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND VARIA AND YPOGEGRAMMENI
   * 1F84; 1F84; 1F8C; 1F0C 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND OXIA AND YPOGEGRAMMENI
   * 1F85; 1F85; 1F8D; 1F0D 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND OXIA AND YPOGEGRAMMENI
   * 1F86; 1F86; 1F8E; 1F0E 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
   * 1F87; 1F87; 1F8F; 1F0F 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
   * 1F88; 1F80; 1F88; 1F08 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND PROSGEGRAMMENI
   * 1F89; 1F81; 1F89; 1F09 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND PROSGEGRAMMENI
   * 1F8A; 1F82; 1F8A; 1F0A 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA AND PROSGEGRAMMENI
   * 1F8B; 1F83; 1F8B; 1F0B 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA AND PROSGEGRAMMENI
   * 1F8C; 1F84; 1F8C; 1F0C 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA AND PROSGEGRAMMENI
   * 1F8D; 1F85; 1F8D; 1F0D 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA AND PROSGEGRAMMENI
   * 1F8E; 1F86; 1F8E; 1F0E 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
   * 1F8F; 1F87; 1F8F; 1F0F 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
   * 1F90; 1F90; 1F98; 1F28 0399; # GREEK SMALL LETTER ETA WITH PSILI AND YPOGEGRAMMENI
   * 1F91; 1F91; 1F99; 1F29 0399; # GREEK SMALL LETTER ETA WITH DASIA AND YPOGEGRAMMENI
   * 1F92; 1F92; 1F9A; 1F2A 0399; # GREEK SMALL LETTER ETA WITH PSILI AND VARIA AND YPOGEGRAMMENI
   * 1F93; 1F93; 1F9B; 1F2B 0399; # GREEK SMALL LETTER ETA WITH DASIA AND VARIA AND YPOGEGRAMMENI
   * 1F94; 1F94; 1F9C; 1F2C 0399; # GREEK SMALL LETTER ETA WITH PSILI AND OXIA AND YPOGEGRAMMENI
   * 1F95; 1F95; 1F9D; 1F2D 0399; # GREEK SMALL LETTER ETA WITH DASIA AND OXIA AND YPOGEGRAMMENI
   * 1F96; 1F96; 1F9E; 1F2E 0399; # GREEK SMALL LETTER ETA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
   * 1F97; 1F97; 1F9F; 1F2F 0399; # GREEK SMALL LETTER ETA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
   * 1F98; 1F90; 1F98; 1F28 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND PROSGEGRAMMENI
   * 1F99; 1F91; 1F99; 1F29 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND PROSGEGRAMMENI
   * 1F9A; 1F92; 1F9A; 1F2A 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA AND PROSGEGRAMMENI
   * 1F9B; 1F93; 1F9B; 1F2B 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA AND PROSGEGRAMMENI
   * 1F9C; 1F94; 1F9C; 1F2C 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA AND PROSGEGRAMMENI
   * 1F9D; 1F95; 1F9D; 1F2D 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA AND PROSGEGRAMMENI
   * 1F9E; 1F96; 1F9E; 1F2E 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
   * 1F9F; 1F97; 1F9F; 1F2F 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
   * 1FA0; 1FA0; 1FA8; 1F68 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND YPOGEGRAMMENI
   * 1FA1; 1FA1; 1FA9; 1F69 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND YPOGEGRAMMENI
   * 1FA2; 1FA2; 1FAA; 1F6A 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND VARIA AND YPOGEGRAMMENI
   * 1FA3; 1FA3; 1FAB; 1F6B 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND VARIA AND YPOGEGRAMMENI
   * 1FA4; 1FA4; 1FAC; 1F6C 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND OXIA AND YPOGEGRAMMENI
   * 1FA5; 1FA5; 1FAD; 1F6D 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND OXIA AND YPOGEGRAMMENI
   * 1FA6; 1FA6; 1FAE; 1F6E 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
   * 1FA7; 1FA7; 1FAF; 1F6F 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
   * 1FA8; 1FA0; 1FA8; 1F68 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND PROSGEGRAMMENI
   * 1FA9; 1FA1; 1FA9; 1F69 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND PROSGEGRAMMENI
   * 1FAA; 1FA2; 1FAA; 1F6A 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA AND PROSGEGRAMMENI
   * 1FAB; 1FA3; 1FAB; 1F6B 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA AND PROSGEGRAMMENI
   * 1FAC; 1FA4; 1FAC; 1F6C 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA AND PROSGEGRAMMENI
   * 1FAD; 1FA5; 1FAD; 1F6D 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA AND PROSGEGRAMMENI
   * 1FAE; 1FA6; 1FAE; 1F6E 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
   * 1FAF; 1FA7; 1FAF; 1F6F 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
   * 1FB3; 1FB3; 1FBC; 0391 0399; # GREEK SMALL LETTER ALPHA WITH YPOGEGRAMMENI
   * 1FBC; 1FB3; 1FBC; 0391 0399; # GREEK CAPITAL LETTER ALPHA WITH PROSGEGRAMMENI
   * 1FC3; 1FC3; 1FCC; 0397 0399; # GREEK SMALL LETTER ETA WITH YPOGEGRAMMENI
   * 1FCC; 1FC3; 1FCC; 0397 0399; # GREEK CAPITAL LETTER ETA WITH PROSGEGRAMMENI
   * 1FF3; 1FF3; 1FFC; 03A9 0399; # GREEK SMALL LETTER OMEGA WITH YPOGEGRAMMENI
   * 1FFC; 1FF3; 1FFC; 03A9 0399; # GREEK CAPITAL LETTER OMEGA WITH PROSGEGRAMMENI
   *
   * # Some characters with YPOGEGRAMMENI also have no corresponding titlecases
   *
   * 1FB2; 1FB2; 1FBA 0345; 1FBA 0399; # GREEK SMALL LETTER ALPHA WITH VARIA AND YPOGEGRAMMENI
   * 1FB4; 1FB4; 0386 0345; 0386 0399; # GREEK SMALL LETTER ALPHA WITH OXIA AND YPOGEGRAMMENI
   * 1FC2; 1FC2; 1FCA 0345; 1FCA 0399; # GREEK SMALL LETTER ETA WITH VARIA AND YPOGEGRAMMENI
   * 1FC4; 1FC4; 0389 0345; 0389 0399; # GREEK SMALL LETTER ETA WITH OXIA AND YPOGEGRAMMENI
   * 1FF2; 1FF2; 1FFA 0345; 1FFA 0399; # GREEK SMALL LETTER OMEGA WITH VARIA AND YPOGEGRAMMENI
   * 1FF4; 1FF4; 038F 0345; 038F 0399; # GREEK SMALL LETTER OMEGA WITH OXIA AND YPOGEGRAMMENI
   *
   * 1FB7; 1FB7; 0391 0342 0345; 0391 0342 0399; # GREEK SMALL LETTER ALPHA WITH PERISPOMENI AND YPOGEGRAMMENI
   * 1FC7; 1FC7; 0397 0342 0345; 0397 0342 0399; # GREEK SMALL LETTER ETA WITH PERISPOMENI AND YPOGEGRAMMENI
   * 1FF7; 1FF7; 03A9 0342 0345; 03A9 0342 0399; # GREEK SMALL LETTER OMEGA WITH PERISPOMENI AND YPOGEGRAMMENI
   */
  lazy val toUpperCase: java.util.HashMap[Char, String] = {
    val cases = new java.util.HashMap[Char, String]()
    cases.put('\u00DF', "\u0053\u0053")       // ß to SS
    cases.put('\u0149', "\u02BC\u004E")       // ŉ to ʼN
    cases.put('\u01F0', "\u004A\u030C")       // ǰ to J̌
    cases.put('\u0390', "\u0399\u0308\u0301") // ΐ to Ϊ́
    cases.put('\u03B0', "\u03A5\u0308\u0301") // ΰ to Ϋ́
    cases.put('\u0587', "\u0535\u0552")       // և to ԵՒ
    cases.put('\u1E96', "\u0048\u0331")       // ẖ to H̱
    cases.put('\u1E97', "\u0054\u0308")       // ẗ to T̈
    cases.put('\u1E98', "\u0057\u030A")       // ẘ to W̊
    cases.put('\u1E99', "\u0059\u030A")       // ẙ to Y̊
    cases.put('\u1E9A', "\u0041\u02BE")       // ẚ to Aʾ
    cases.put('\u1F50', "\u03A5\u0313")       // ὐ to Υ̓
    cases.put('\u1F52', "\u03A5\u0313\u0300") // ὒ to Υ̓̀
    cases.put('\u1F54', "\u03A5\u0313\u0301") // ὔ to Υ̓́
    cases.put('\u1F56', "\u03A5\u0313\u0342") // ὖ to Υ̓͂
    cases.put('\u1F80', "\u1F08\u0399")       // ᾀ to ἈΙ
    cases.put('\u1F81', "\u1F09\u0399")       // ᾁ to ἉΙ
    cases.put('\u1F82', "\u1F0A\u0399")       // ᾂ to ἊΙ
    cases.put('\u1F83', "\u1F0B\u0399")       // ᾃ to ἋΙ
    cases.put('\u1F84', "\u1F0C\u0399")       // ᾄ to ἌΙ
    cases.put('\u1F85', "\u1F0D\u0399")       // ᾅ to ἍΙ
    cases.put('\u1F86', "\u1F0E\u0399")       // ᾆ to ἎΙ
    cases.put('\u1F87', "\u1F0F\u0399")       // ᾇ to ἏΙ
    cases.put('\u1F88', "\u1F08\u0399")       // ᾈ to ἈΙ
    cases.put('\u1F89', "\u1F09\u0399")       // ᾉ to ἉΙ
    cases.put('\u1F8A', "\u1F0A\u0399")       // ᾊ to ἊΙ
    cases.put('\u1F8B', "\u1F0B\u0399")       // ᾋ to ἋΙ
    cases.put('\u1F8C', "\u1F0C\u0399")       // ᾌ to ἌΙ
    cases.put('\u1F8D', "\u1F0D\u0399")       // ᾍ to ἍΙ
    cases.put('\u1F8E', "\u1F0E\u0399")       // ᾎ to ἎΙ
    cases.put('\u1F8F', "\u1F0F\u0399")       // ᾏ to ἏΙ
    cases.put('\u1F90', "\u1F28\u0399")       // ᾐ to ἨΙ
    cases.put('\u1F91', "\u1F29\u0399")       // ᾑ to ἩΙ
    cases.put('\u1F92', "\u1F2A\u0399")       // ᾒ to ἪΙ
    cases.put('\u1F93', "\u1F2B\u0399")       // ᾓ to ἫΙ
    cases.put('\u1F94', "\u1F2C\u0399")       // ᾔ to ἬΙ
    cases.put('\u1F95', "\u1F2D\u0399")       // ᾕ to ἭΙ
    cases.put('\u1F96', "\u1F2E\u0399")       // ᾖ to ἮΙ
    cases.put('\u1F97', "\u1F2F\u0399")       // ᾗ to ἯΙ
    cases.put('\u1F98', "\u1F28\u0399")       // ᾘ to ἨΙ
    cases.put('\u1F99', "\u1F29\u0399")       // ᾙ to ἩΙ
    cases.put('\u1F9A', "\u1F2A\u0399")       // ᾚ to ἪΙ
    cases.put('\u1F9B', "\u1F2B\u0399")       // ᾛ to ἫΙ
    cases.put('\u1F9C', "\u1F2C\u0399")       // ᾜ to ἬΙ
    cases.put('\u1F9D', "\u1F2D\u0399")       // ᾝ to ἭΙ
    cases.put('\u1F9E', "\u1F2E\u0399")       // ᾞ to ἮΙ
    cases.put('\u1F9F', "\u1F2F\u0399")       // ᾟ to ἯΙ
    cases.put('\u1FA0', "\u1F68\u0399")       // ᾠ to ὨΙ
    cases.put('\u1FA1', "\u1F69\u0399")       // ᾡ to ὩΙ
    cases.put('\u1FA2', "\u1F6A\u0399")       // ᾢ to ὪΙ
    cases.put('\u1FA3', "\u1F6B\u0399")       // ᾣ to ὫΙ
    cases.put('\u1FA4', "\u1F6C\u0399")       // ᾤ to ὬΙ
    cases.put('\u1FA5', "\u1F6D\u0399")       // ᾥ to ὭΙ
    cases.put('\u1FA6', "\u1F6E\u0399")       // ᾦ to ὮΙ
    cases.put('\u1FA7', "\u1F6F\u0399")       // ᾧ to ὯΙ
    cases.put('\u1FA8', "\u1F68\u0399")       // ᾨ to ὨΙ
    cases.put('\u1FA9', "\u1F69\u0399")       // ᾩ to ὩΙ
    cases.put('\u1FAA', "\u1F6A\u0399")       // ᾪ to ὪΙ
    cases.put('\u1FAB', "\u1F6B\u0399")       // ᾫ to ὫΙ
    cases.put('\u1FAC', "\u1F6C\u0399")       // ᾬ to ὬΙ
    cases.put('\u1FAD', "\u1F6D\u0399")       // ᾭ to ὭΙ
    cases.put('\u1FAE', "\u1F6E\u0399")       // ᾮ to ὮΙ
    cases.put('\u1FAF', "\u1F6F\u0399")       // ᾯ to ὯΙ
    cases.put('\u1FB2', "\u1FBA\u0399")       // ᾲ to ᾺΙ
    cases.put('\u1FB3', "\u0391\u0399")       // ᾳ to ΑΙ
    cases.put('\u1FB4', "\u0386\u0399")       // ᾴ to ΆΙ
    cases.put('\u1FB6', "\u0391\u0342")       // ᾶ to Α͂
    cases.put('\u1FB7', "\u0391\u0342\u0399") // ᾷ to Α͂Ι
    cases.put('\u1FBC', "\u0391\u0399")       // ᾼ to ΑΙ
    cases.put('\u1FC2', "\u1FCA\u0399")       // ῂ to ῊΙ
    cases.put('\u1FC3', "\u0397\u0399")       // ῃ to ΗΙ
    cases.put('\u1FC4', "\u0389\u0399")       // ῄ to ΉΙ
    cases.put('\u1FC6', "\u0397\u0342")       // ῆ to Η͂
    cases.put('\u1FC7', "\u0397\u0342\u0399") // ῇ to Η͂Ι
    cases.put('\u1FCC', "\u0397\u0399")       // ῌ to ΗΙ
    cases.put('\u1FD2', "\u0399\u0308\u0300") // ῒ to Ϊ̀
    cases.put('\u1FD3', "\u0399\u0308\u0301") // ΐ to Ϊ́
    cases.put('\u1FD6', "\u0399\u0342")       // ῖ to Ι͂
    cases.put('\u1FD7', "\u0399\u0308\u0342") // ῗ to Ϊ͂
    cases.put('\u1FE2', "\u03A5\u0308\u0300") // ῢ to Ϋ̀
    cases.put('\u1FE3', "\u03A5\u0308\u0301") // ΰ to Ϋ́
    cases.put('\u1FE4', "\u03A1\u0313")       // ῤ to Ρ̓
    cases.put('\u1FE6', "\u03A5\u0342")       // ῦ to Υ͂
    cases.put('\u1FE7', "\u03A5\u0308\u0342") // ῧ to Ϋ͂
    cases.put('\u1FF2', "\u1FFA\u0399")       // ῲ to ῺΙ
    cases.put('\u1FF3', "\u03A9\u0399")       // ῳ to ΩΙ
    cases.put('\u1FF4', "\u038F\u0399")       // ῴ to ΏΙ
    cases.put('\u1FF6', "\u03A9\u0342")       // ῶ to Ω͂
    cases.put('\u1FF7', "\u03A9\u0342\u0399") // ῷ to Ω͂Ι
    cases.put('\u1FFC', "\u03A9\u0399")       // ῼ to ΩΙ
    cases.put('\uFB00', "\u0046\u0046")       // ﬀ to FF
    cases.put('\uFB01', "\u0046\u0049")       // ﬁ to FI
    cases.put('\uFB02', "\u0046\u004C")       // ﬂ to FL
    cases.put('\uFB03', "\u0046\u0046\u0049") // ﬃ to FFI
    cases.put('\uFB04', "\u0046\u0046\u004C") // ﬄ to FFL
    cases.put('\uFB05', "\u0053\u0054")       // ﬅ to ST
    cases.put('\uFB06', "\u0053\u0054")       // ﬆ to ST
    cases.put('\uFB13', "\u0544\u0546")       // ﬓ to ՄՆ
    cases.put('\uFB14', "\u0544\u0535")       // ﬔ to ՄԵ
    cases.put('\uFB15', "\u0544\u053B")       // ﬕ to ՄԻ
    cases.put('\uFB16', "\u054E\u0546")       // ﬖ to ՎՆ
    cases.put('\uFB17', "\u0544\u053D")       // ﬗ to ՄԽ
    cases
  }
}
