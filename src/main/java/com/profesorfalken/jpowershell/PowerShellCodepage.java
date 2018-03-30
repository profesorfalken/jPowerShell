/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.profesorfalken.jpowershell;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Enum that contains possible CodePage values needed to correctly set the encoding in a windows console session<br>
 * https://msdn.microsoft.com/de-de/library/windows/desktop/dd317756(v=vs.85).aspx
 *
 * @author Harinus
 */
class PowerShellCodepage {

    private static final Map<String, String> codePages = new HashMap<>();

    static {
        codePages.put("37", "IBM037");
        codePages.put("437", "IBM437");
        codePages.put("500", "IBM500");
        codePages.put("708", "ASMO-708");
        codePages.put("709", "");
        codePages.put("710", "");
        codePages.put("720", "DOS-720");
        codePages.put("737", "ibm737");
        codePages.put("775", "ibm775");
        codePages.put("850", "ibm850");
        codePages.put("852", "ibm852");
        codePages.put("855", "IBM855");
        codePages.put("857", "ibm857");
        codePages.put("858", "IBM00858");
        codePages.put("860", "IBM860");
        codePages.put("861", "ibm861");
        codePages.put("862", "DOS-862");
        codePages.put("863", "IBM863");
        codePages.put("864", "IBM864");
        codePages.put("865", "IBM865");
        codePages.put("866", "cp866");
        codePages.put("869", "ibm869");
        codePages.put("870", "IBM870");
        codePages.put("874", "windows-874");
        codePages.put("875", "cp875");
        codePages.put("932", "shift_jis");
        codePages.put("936", "gb2312");
        codePages.put("949", "ks_c_5601-1987");
        codePages.put("950", "big5");
        codePages.put("1026", "IBM1026");
        codePages.put("1047", "IBM01047");
        codePages.put("1140", "IBM01140");
        codePages.put("1141", "IBM01141");
        codePages.put("1142", "IBM01142");
        codePages.put("1143", "IBM01143");
        codePages.put("1144", "IBM01144");
        codePages.put("1145", "IBM01145");
        codePages.put("1146", "IBM01146");
        codePages.put("1147", "IBM01147");
        codePages.put("1148", "IBM01148");
        codePages.put("1149", "IBM01149");
        codePages.put("1200", "utf-16");
        codePages.put("1201", "unicodeFFFE");
        codePages.put("1250", "windows-1250");
        codePages.put("1251", "windows-1251");
        codePages.put("1252", "windows-1252");
        codePages.put("1253", "windows-1253");
        codePages.put("1254", "windows-1254");
        codePages.put("1255", "windows-1255");
        codePages.put("1256", "windows-1256");
        codePages.put("1257", "windows-1257");
        codePages.put("1258", "windows-1258");
        codePages.put("1361", "Johab");
        codePages.put("10000", "macintosh");
        codePages.put("10001", "x-mac-japanese");
        codePages.put("10002", "x-mac-chinesetrad");
        codePages.put("10003", "x-mac-korean");
        codePages.put("10004", "x-mac-arabic");
        codePages.put("10005", "x-mac-hebrew");
        codePages.put("10006", "x-mac-greek");
        codePages.put("10007", "x-mac-cyrillic");
        codePages.put("10008", "x-mac-chinesesimp");
        codePages.put("10010", "x-mac-romanian");
        codePages.put("10017", "x-mac-ukrainian");
        codePages.put("10021", "x-mac-thai");
        codePages.put("10029", "x-mac-ce");
        codePages.put("10079", "x-mac-icelandic");
        codePages.put("10081", "x-mac-turkish");
        codePages.put("10082", "x-mac-croatian");
        codePages.put("12000", "utf-32");
        codePages.put("12001", "utf-32BE");
        codePages.put("20000", "x-Chinese_CNS");
        codePages.put("20001", "x-cp20001");
        codePages.put("20002", "x_Chinese-Eten");
        codePages.put("20003", "x-cp20003");
        codePages.put("20004", "x-cp20004");
        codePages.put("20005", "x-cp20005");
        codePages.put("20105", "x-IA5");
        codePages.put("20106", "x-IA5-German");
        codePages.put("20107", "x-IA5-Swedish");
        codePages.put("20108", "x-IA5-Norwegian");
        codePages.put("20127", "us-ascii");
        codePages.put("20261", "x-cp20261");
        codePages.put("20269", "x-cp20269");
        codePages.put("20273", "IBM273");
        codePages.put("20277", "IBM277");
        codePages.put("20278", "IBM278");
        codePages.put("20280", "IBM280");
        codePages.put("20284", "IBM284");
        codePages.put("20285", "IBM285");
        codePages.put("20290", "IBM290");
        codePages.put("20297", "IBM297");
        codePages.put("20420", "IBM420");
        codePages.put("20423", "IBM423");
        codePages.put("20424", "IBM424");
        codePages.put("20833", "x-EBCDIC-KoreanExtended");
        codePages.put("20838", "IBM-Thai");
        codePages.put("20866", "koi8-r");
        codePages.put("20871", "IBM871");
        codePages.put("20880", "IBM880");
        codePages.put("20905", "IBM905");
        codePages.put("20924", "IBM00924");
        codePages.put("20932", "EUC-JP");
        codePages.put("20936", "x-cp20936");
        codePages.put("20949", "x-cp20949");
        codePages.put("21025", "cp1025");
        codePages.put("21027", "");
        codePages.put("21866", "koi8-u");
        codePages.put("28591", "iso-8859-1");
        codePages.put("28592", "iso-8859-2");
        codePages.put("28593", "iso-8859-3");
        codePages.put("28594", "iso-8859-4");
        codePages.put("28595", "iso-8859-5");
        codePages.put("28596", "iso-8859-6");
        codePages.put("28597", "iso-8859-7");
        codePages.put("28598", "iso-8859-8");
        codePages.put("28599", "iso-8859-9");
        codePages.put("28603", "iso-8859-13");
        codePages.put("28605", "iso-8859-15");
        codePages.put("29001", "x-Europa");
        codePages.put("38598", "iso-8859-8-i");
        codePages.put("50220", "iso-2022-jp");
        codePages.put("50221", "csISO2022JP");
        codePages.put("50222", "iso-2022-jp");
        codePages.put("50225", "iso-2022-kr");
        codePages.put("50227", "x-cp50227");
        codePages.put("50229", "");
        codePages.put("50930", "");
        codePages.put("50931", "");
        codePages.put("50933", "");
        codePages.put("50935", "");
        codePages.put("50936", "");
        codePages.put("50937", "");
        codePages.put("50939", "");
        codePages.put("51932", "euc-jp");
        codePages.put("51936", "EUC-CN");
        codePages.put("51949", "euc-kr");
        codePages.put("51950", "");
        codePages.put("52936", "hz-gb-2312");
        codePages.put("54936", "GB18030");
        codePages.put("57002", "x-iscii-de");
        codePages.put("57003", "x-iscii-be");
        codePages.put("57004", "x-iscii-ta");
        codePages.put("57005", "x-iscii-te");
        codePages.put("57006", "x-iscii-as");
        codePages.put("57007", "x-iscii-or");
        codePages.put("57008", "x-iscii-ka");
        codePages.put("57009", "x-iscii-ma");
        codePages.put("57010", "x-iscii-gu");
        codePages.put("57011", "x-iscii-pa");
        codePages.put("65000", "utf-7");
        codePages.put("65001", "utf-8");
    }

    /**
     * Get the encoding value from CodePage
     *
     * @param cpIdentifier encoding value
     * @return String The codepage name
     */
    public static String getCodePageNameByIdetifier(String cpIdentifier) {
        return codePages.get(cpIdentifier);
    }

    /**
     * Get the CodePage code from encoding value
     *
     * @param cpName the codepage name
     * @return String the identifier
     */
    public static String getIdentifierByCodePageName(String cpName) {
        if (cpName != null) {
            for (Entry<String, String> codePage : codePages.entrySet()) {
                if (codePage.getValue().toLowerCase().equals(cpName.toLowerCase())) {
                    return codePage.getKey();
                }
            }
        }
        //Default UTF-8
        return "65001";
    }

}
