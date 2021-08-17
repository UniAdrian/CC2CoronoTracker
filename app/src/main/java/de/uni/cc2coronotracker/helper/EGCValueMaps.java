package de.uni.cc2coronotracker.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the datasets otherwise provided as JSON files as hashmaps
 * Would have loved to fetch and cache those from the web to keep up to date,
 * but i am really time constrained right now and just want to get as many
 * features working as possible...
 */
public class EGCValueMaps {

    public static final Map<String, String> DISEASE_AGENT_TARGETED = new HashMap<>() {{
        put("840539006", "COVID-19");
    }};

    public static final Map<String, String> VACCINE_PROPHYLAXIS = new HashMap<>() {{
        put("1119349007", "SARS-CoV-2 mRNA vaccine");
        put("1119305005", "CSARS-CoV-2 antigen vaccine");
        put("J07BX03", "covid-19 vaccines");
    }};

    public static final Map<String, String> VACCINE_MEDICINAL_PRODUCT = new HashMap<>() {{
        put("EU/1/20/1528", "Comirnaty");
        put("EU/1/20/1507", "COVID-19 Vaccine Moderna");
        put("EU/1/21/1529", "Vaxzevria");
        put("EU/1/20/1525", "COVID-19 Vaccine Janssen");
        put("CVnCoV", "CVnCoV");
        put("Sputnik-V", "Sputnik-V");
        put("Convidecia", "Convidecia");
        put("EpiVacCorona", "EpiVacCorona");
        put("BBIBP-CorV", "BBIBP-CorV");
        put("Inactivated-SARS-CoV-2-Vero-Cell", "Inactivated SARS-CoV-2 (Vero Cell)");
        put("CoronaVac", "CoronaVac");
        put("Covaxin", "Covaxin (also known as BBV152 A, B, C)");
    }};

    public static final Map<String, String> VACCINE_MAH_MANF = new HashMap<>() {{
        put("ORG-100001699", "AstraZeneca AB");
        put("ORG-100030215", "Biontech Manufacturing GmbH");
        put("ORG-100001417", "Janssen-Cilag International");
        put("ORG-100031184", "Moderna Biotech Spain S.L.");
        put("ORG-100006270", "Curevac AG");
        put("ORG-100013793", "CanSino Biologics");
        put("ORG-100020693", "China Sinopharm International Corp. - Beijing location");
        put("ORG-100010771", "Sinopharm Weiqida Europe Pharmaceutical s.r.o. - Prague location");
        put("ORG-100024420", "Sinopharm Zhijun (Shenzhen) Pharmaceutical Co. Ltd. - Shenzhen location");
        put("ORG-100032020", "Novavax CZ AS");
        put("Gamaleya-Research-Institute", "Gamaleya Research Institute");
        put("Vector-Institute", "Vector Institute");
        put("Sinovac-Biotech", "Sinovac Biotech");
        put("Bharat-Biotech", "Bharat Biotech");
    }};

    public static final Map<String, String> COUNTRY_2_CODES_EN = new HashMap<>() {{
        put("0", "Switzerland");
        put("1", "Afghanistan");
        put("2", "Egypt");
        put("3", "Åland Islands");
        put("4", "Albania");
        put("5", "Algeria");
        put("6", "American Samoa");
        put("7", "Virgin Islands (U.S.)");
        put("8", "Andorra");
        put("9", "Angola");
        put("10", "Anguilla");
        put("11", "Antarctica");
        put("12", "Antigua and Barbuda");
        put("13", "Equatorial Guinea");
        put("14", "Argentina");
        put("15", "Armenia");
        put("16", "Aruba");
        put("17", "Azerbaijan");
        put("18", "Ethiopia");
        put("19", "Australia");
        put("20", "Bahamas");
        put("21", "Bahrain");
        put("22", "Bangladesh");
        put("23", "Barbados");
        put("24", "Belarus");
        put("25", "Belgium");
        put("26", "Belize");
        put("27", "Benin");
        put("28", "Bermuda");
        put("29", "Bhutan");
        put("30", "Bolivia");
        put("31", "Bonaire Sint Eustatius and Saba");
        put("32", "Bosnia and Herzegovina");
        put("33", "Botswana");
        put("34", "Bouvet Island");
        put("35", "Brazil");
        put("36", "Virgin Islands (British)");
        put("37", "British Indian Ocean Territory");
        put("38", "Brunei Darussalam");
        put("39", "Bulgaria");
        put("40", "Burkina Faso");
        put("41", "Burundi");
        put("42", "Chile");
        put("43", "China");
        put("44", "Cook Islands");
        put("45", "Costa Rica");
        put("46", "Curaçao");
        put("47", "Denmark");
        put("48", "Germany");
        put("49", "Dominica");
        put("50", "Dominican Republic");
        put("51", "Djibouti");
        put("52", "Ecuador");
        put("53", "Côte d'Ivoire");
        put("54", "El Salvador");
        put("55", "Eritrea");
        put("56", "Estonia");
        put("57", "Eswatini");
        put("58", "Falkland Islands");
        put("59", "Faroe Islands");
        put("60", "Fiji");
        put("61", "Finland");
        put("62", "France");
        put("63", "French Guiana");
        put("64", "French Polynesia");
        put("65", "French Southern Territories");
        put("66", "Gabon");
        put("67", "Gambia");
        put("68", "Georgia");
        put("69", "Ghana");
        put("70", "Gibraltar");
        put("71", "Grenada");
        put("72", "Greece");
        put("73", "Greenland");
        put("74", "Guadeloupe");
        put("75", "Guam");
        put("76", "Guatemala");
        put("77", "Guernsey");
        put("78", "Guinea");
        put("79", "Guinea-Bissau");
        put("80", "Guyana");
        put("81", "Haiti");
        put("82", "Heard Island and McDonald Islands");
        put("83", "Honduras");
        put("84", "Hong Kong");
        put("85", "India");
        put("86", "Indonesia");
        put("87", "Isle of Man");
        put("88", "Iraq");
        put("89", "Iran");
        put("90", "Ireland");
        put("91", "Iceland");
        put("92", "Israel");
        put("93", "Italy");
        put("94", "Jamaica");
        put("95", "Japan");
        put("96", "Yemen");
        put("97", "Jersey");
        put("98", "Jordan");
        put("99", "Cayman Islands");
        put("100", "Cambodia");
        put("101", "Cameroon");
        put("102", "Canada");
        put("103", "Cabo Verde");
        put("104", "Kazakhstan");
        put("105", "Qatar");
        put("106", "Kenya");
        put("107", "Kyrgyzstan");
        put("108", "Kiribati");
        put("109", "Cocos Islands");
        put("110", "Colombia");
        put("111", "Comoros");
        put("112", "Congo Democratic Republic of the");
        put("113", "Congo");
        put("114", "Korea (Democratic People's Republic of)");
        put("115", "Korea Republic of");
        put("116", "Croatia");
        put("117", "Cuba");
        put("118", "Kosovo");
        put("119", "Kuwait");
        put("120", "Lao People's Democratic Republic");
        put("121", "Lesotho");
        put("122", "Latvia");
        put("123", "Lebanon");
        put("124", "Liberia");
        put("125", "Libya");
        put("126", "Liechtenstein");
        put("127", "Lithuania");
        put("128", "Luxembourg");
        put("129", "Macao");
        put("130", "Madagascar");
        put("131", "Malawi");
        put("132", "Malaysia");
        put("133", "Maldives");
        put("134", "Mali");
        put("135", "Malta");
        put("136", "Morocco");
        put("137", "Marshall Islands");
        put("138", "Martinique");
        put("139", "Mauritania");
        put("140", "Mauritius");
        put("141", "Mayotte");
        put("142", "Mexico");
        put("143", "Micronesia (Federated States of)");
        put("144", "Moldova Republic of");
        put("145", "Monaco");
        put("146", "Mongolia");
        put("147", "Montenegro");
        put("148", "Montserrat");
        put("149", "Mozambique");
        put("150", "Myanmar");
        put("151", "Namibia");
        put("152", "Nauru");
        put("153", "Nepal");
        put("154", "New Caledonia");
        put("155", "New Zealand");
        put("156", "Nicaragua");
        put("157", "Netherlands");
        put("158", "Niger");
        put("159", "Nigeria");
        put("160", "Niue");
        put("161", "Northern Mariana Islands");
        put("162", "North Macedonia");
        put("163", "Norfolk Island");
        put("164", "Norway");
        put("165", "Oman");
        put("166", "Austria");
        put("167", "Timor-Leste");
        put("168", "Pakistan");
        put("169", "Palestine State of");
        put("170", "Palau");
        put("171", "Panama");
        put("172", "Papua New Guinea");
        put("173", "Paraguay");
        put("174", "Peru");
        put("175", "Philippines");
        put("176", "Pitcairn");
        put("177", "Poland");
        put("178", "Portugal");
        put("179", "Puerto Rico");
        put("180", "Réunion");
        put("181", "Rwanda");
        put("182", "Romania");
        put("183", "Russian Federation");
        put("184", "Solomon Islands");
        put("185", "Saint Barthélemy");
        put("186", "Saint Martin (French part)");
        put("187", "Zambia");
        put("188", "Samoa");
        put("189", "San Marino");
        put("190", "Sao Tome and Principe");
        put("191", "Saudi Arabia");
        put("192", "Sweden");
        put("193", "Senegal");
        put("194", "Serbia");
        put("195", "Seychelles");
        put("196", "Sierra Leone");
        put("197", "Zimbabwe");
        put("198", "Singapore");
        put("199", "Sint Maarten (Dutch part)");
        put("200", "Slovakia");
        put("201", "Slovenia");
        put("202", "Somalia");
        put("203", "Spain");
        put("204", "Sri Lanka");
        put("205", "Saint Helena Ascension and Tristan da Cunha");
        put("206", "Saint Kitts and Nevis");
        put("207", "Saint Lucia");
        put("208", "Saint Pierre and Miquelon");
        put("209", "Saint Vincent and the Grenadines");
        put("210", "South Africa");
        put("211", "Sudan");
        put("212", "South Georgia and the South Sandwich Islands");
        put("213", "South Sudan");
        put("214", "Suriname");
        put("215", "Svalbard and Jan Mayen");
        put("216", "Syrian Arab Republic");
        put("217", "Tajikistan");
        put("218", "Taiwan");
        put("219", "Tanzania United Republic of");
        put("220", "Thailand");
        put("221", "Togo");
        put("222", "Tokelau");
        put("223", "Tonga");
        put("224", "Trinidad and Tobago");
        put("225", "Chad");
        put("226", "Czechia");
        put("227", "Tunisia");
        put("228", "Turkey");
        put("229", "Turkmenistan");
        put("230", "Turks and Caicos Islands");
        put("231", "Tuvalu");
        put("232", "Uganda");
        put("233", "Ukraine");
        put("234", "Hungary");
        put("235", "United States Minor Outlying Islands");
        put("236", "Uruguay");
        put("237", "Uzbekistan");
        put("238", "Vanuatu");
        put("239", "Holy See");
        put("240", "Venezuela (Bolivarian Republic of)");
        put("241", "United Arab Emirates");
        put("242", "United States of America");
        put("243", "United Kingdom of Great Britain and Northern Ireland");
        put("244", "Viet Nam");
        put("245", "Wallis and Futuna");
        put("246", "Christmas Island");
        put("247", "Western Sahara");
        put("248", "Central African Republic");
        put("249", "Cyprus");
    }};

    public static final Map<String, String> TEST_TYPE = new HashMap<>() {{
        put("LP6464-4", "Nucleic acid amplification with probe detection");
        put("LP217198-3", "Rapid immunoassay");
    }};

    public static final Map<String, String> TEST_RESULT = new HashMap<>() {{
        put("260373001", "Detected");
        put("260415000", "Not detected");
    }};

    public static final Map<String, String> TEST_DEVICES = new HashMap<>() {{
        put("1833", "COVID-VIRO");
        put("1232", "Panbio Covid-19 Ag Rapid Test");
        put("1457", "SARS-CoV-2 Antigen Rapid Test");
        put("1468", "Flowflex SARS-CoV-2 Antigen rapid test");
        put("2108", "AESKU.RAPID SARS-CoV-2");
        put("2130", "TestNOW® - COVID-19 Antigen Test");
        put("1304", "AMP Rapid Test SARS-CoV-2 Ag");
        put("1822", "Rapid COVID-19 Antigen Test(Colloidal Gold)");
        put("1815", "COVID-19 (SARS-CoV-2) Antigen Test Kit (Colloidal Gold) - Nasal Swab");
        put("1736", "COVID-19 (SARS-CoV-2) Antigen Test Kit(Colloidal Gold)");
        put("768", "mariPOC SARS-CoV-2");
        put("2079", "mariPOC Quick Flu+");
        put("2078", "mariPOC Respi+");
        put("1618", "Artron COVID-19 Antigen Test");
        put("1654", "Asan Easy Test COVID-19 Ag");
        put("770", "ECOTEST COVID-19 Antigen Rapid Test Device");
        put("2350", "ECOTEST COVID-19 Antigen Rapid Test Device");
        put("2010", "NOVA Test® SARS-CoV-2 Antigen Rapid Test Kit (Colloidal Gold Immunochromatography)");
        put("1800", "Ksmart® SARS-COV2 Antigen Rapid Test");
        put("2101", "COVID-19 Antigen Rapid Test");
        put("1906", "COVID-19 Antigen Rapid Test Device");
        put("1065", "BD Veritor™ System for Rapid Detection of SARS CoV 2");
        put("1870", "Novel Coronavirus 2019-nCoV Antigen Test (Colloidal Gold)");
        put("2072", "Novel Coronavirus (SARS-CoV-2) Antigen Rapid Test Kit");
        put("1331", "SARS-CoV-2 Antigen Rapid Test Kit");
        put("1485", "WANTAI SARS-CoV-2 Ag Rapid Test (Colloidal Gold)");
        put("1484", "Wantai SARS-CoV-2 Ag Rapid Test (FIA)");
        put("2031", "Coronavirus Ag Rapid Test Cassette (Swab)");
        put("2247", "CoviGnost AG Test Device 1x20");
        put("1286", "SARS-CoV-2 Antigen Rapid Test Kit (Fluorescence Immunochromatography)");
        put("2035", "SARS-CoV-2 Ag Rapid Test");
        put("1599", "Biomerica COVID-19 Antigen Rapid Test (nasopharyngeal swab)");
        put("1242", "NowCheck COVID-19 Ag Test");
        put("1223", "BIOSYNEX COVID-19 Ag BSS");
        put("1494", "BIOSYNEX COVID-19 Ag+ BSS");
        put("2067", "SARS-CoV-2 Antigen Test Kit (colloidal gold method)");
        put("2013", "biotical SARS-CoV-2 Ag Card");
        put("1989", "AFIAS COVID-19 Ag");
        put("1236", "Rapid Response COVID-19 Antigen Rapid Test");
        put("1173", "CerTest SARS-CoV-2 Card test");
        put("1919", "Coretests COVID-19 Ag Test");
        put("1581", "OnSite COVID-19 Ag Rapid Test");
        put("1225", "Test Rapid Covid-19 Antigen (tampon nazofaringian)");
        put("1375", "DIAQUICK COVID-19 Ag Cassette");
        put("2242", "COVID-19 Antigen Detection Kit");
        put("1243", "ActivXpress+ COVID-19 Antigen Complete Testing Kit");
        put("1739", "EBS SARS-CoV-2 Ag Rapid Test");
        put("2147", "ESPLINE SARS-CoV-2");
        put("1855", "GA CoV-2 Antigen Rapid Test");
        put("1244", "Genbody COVID-19 Ag Test");
        put("2012", "SARS-CoV-2 Antigen Test Kit (Colloidal Gold)");
        put("1253", "GenSure COVID-19 Antigen Rapid Kit");
        put("1820", "SARS-CoV-2 Antigen (Colloidal Gold)");
        put("2183", "One Step Test for SARS-CoV-2 Antigen (Colloidal Gold)");
        put("1197", "SARS-CoV-2 Antigen Kit (Colloidal Gold)");
        put("1144", "GENEDIA W COVID-19 Ag");
        put("1747", "2019-nCoV Antigen Test Kit (colloidal gold method)");
        put("1216", "COVID-19 Ag Rapid Test Kit (Immuno-Chromatography)");
        put("1360", "COVID-19 Ag Test Kit");
        put("1324", "V-CHEK, 2019-nCoV Ag Rapid Test Kit (Immunochromatography)");
        put("1437", "Wondfo 2019-nCoV Antigen Test (Lateral Flow Method)");
        put("1257", "COVID-19 Antigen Rapid Test");
        put("1610", "COVID-19 Antigen Rapid Test Cassette");
        put("1363", "Covid-19 Antigen Rapid Test Kit");
        put("1365", "COVID-19/Influenza A+B Antigen Combo Rapid Test");
        put("1844", "Immunobio SARS-CoV-2 Antigen ANTERIOR NASAL Rapid Test Kit (minimal invasive)");
        put("2317", "SARS-CoV-2 Antigen Rapid Test");
        put("1215", "LYHER Novel Coronavirus (COVID-19) Antigen Test Kit(Colloidal Gold)");
        put("2139", "COVID-19 Antigen Rapid Test Device（Colloidal Gold）");
        put("1392", "COVID-19 Antigen Test Cassette");
        put("1767", "Coronavirus Ag Rapid Test Cassette");
        put("1759", "SARS-CoV-2 Antigen Test Kit");
        put("1263", "Humasis COVID-19 Ag Test");
        put("2107", "Novel Corona Virus (SARS-CoV-2) Ag Rapid Test Kit");
        put("1920", "COVID-19 Antigen Rapid Test Cassette (Colloidal Gold)");
        put("2006", "SARS-CoV-2 antigen Test Kit (LFIA)");
        put("1333", "COVID-19 Rapid Antigen Test (Colloidal Gold)");
        put("1764", "SARS-CoV-2 Antigen Rapid Test Kit (Colloidal Gold)");
        put("1266", "SARS-CoV-2 Antigen Rapid Test Kit");
        put("2128", "PocRoc®SARS-CoV-2 Antigen Rapid Test Kit (Colloidal Gold)");
        put("1267", "QuickProfile COVID-19 Antigen Test");
        put("1268", "LumiraDx SARS-CoV-2 Ag Test");
        put("1180", "MEDsan SARS-CoV-2 Antigen Rapid Test");
        put("2029", "SARS-CoV-2 Antigen Rapid Test Cassette");
        put("1775", "MEXACARE COVID-19 Antigen Rapid Test");
        put("1190", "mö-screen Corona Antigen Test");
        put("1481", "Rapid SARS-CoV-2 Antigen Test Card");
        put("2104", "NADAL COVID -19 Ag +Influenza A/B Test");
        put("1162", "NADAL COVID-19 Ag Test");
        put("1420", "FREND COVID-19 Ag");
        put("2200", "NanoRepro SARS-CoV-2 Antigen Rapid Test");
        put("2241", "MARESKIT");
        put("1501", "COVID-19 Antigen Detection Kit");
        put("1762", "SARS CoV-2 Antigen Rapid Test");
        put("1199", "CAT");
        put("308", "PCL COVID19 Ag Rapid FIA");
        put("2243", "PCL COVID19 Ag Gold");
        put("2116", "SARS-CoV-2 Antigen Detection Kit (Colloidal Gold Immunochromatographic Assay)");
        put("1271", "Exdia COVID-19 Ag");
        put("1495", "Rapid Test Ag 2019-nCov");
        put("1341", "SARS-CoV-2 Antigen Rapid Test (Immunochromatography)");
        put("1097", "Sofia SARS Antigen FIA");
        put("2290", "LIAISON® Quick Detect Covid Ag Assay");
        put("1606", "BIOCREDIT COVID-19 Ag - SARS-CoV 2 Antigen test");
        put("1604", "SARS-CoV-2 Rapid Antigen Test");
        put("2228", "SARS-CoV-2 Rapid Antigen Test Nasal");
        put("1489", "COVID-19 Antigen Rapid Test Kit (Swab)");
        put("1490", "Multi-Respiratory Virus Antigen Test Kit(Swab)  (Influenza A+B/ COVID-19)");
        put("1201", "ScheBo SARS CoV-2 Quick Antigen");
        put("344", "STANDARD F COVID-19 Ag FIA");
        put("345", "STANDARD Q COVID-19 Ag Test");
        put("2052", "STANDARD Q COVID-19 Ag Test Nasal");
        put("1319", "V-Chek SARS-CoV-2 Ag Rapid Test Kit (Colloidal Gold)");
        put("1357", "V-Chek SARS-CoV-2 Rapid Ag Test (colloidal gold)");
        put("2109", "Green Spring SARS-CoV-2 Antigen-Rapid test-Set");
        put("1967", "SARS-CoV-2 Antigen Test Kit (Colloidal Gold Chromatographic Immunoassay)");
        put("1178", "SARS-CoV-2 Spike Protein Test Kit (Colloidal Gold Chromatographic Immunoassay)");
        put("2017", "SARS-CoV-2 Antigen Test Kit");
        put("1769", "SARS-CoV-2 Ag Diagnostic Test Kit (Colloidal Gold)");
        put("1768", "SARS-CoV-2 Ag Diagnostic Test Kit (Immuno-fluorescence)");
        put("1574", "Zhenrui ®COVID-19 Antigen Test Cassette");
        put("1218", "CLINITEST Rapid Covid-19 Antigen Test");
        put("1114", "SGTi-flex COVID-19 Ag");
        put("1466", "TODA CORONADIAG Ag");
        put("1934", "Coronavirus (SARS-CoV 2) Antigen - Oral Fluid");
        put("2074", "SARS-CoV-2 Antigen Rapid Test Kit");
        put("1465", "SARS-CoV-2 Antigen Rapid Test Kit");
        put("1443", "RapidFor SARS-CoV-2 Rapid Ag Test");
        put("2103", "VivaDiag Pro SARS-CoV-2 Ag Rapid Test");
        put("2098", "COVID-19 (SARS-CoV-2) Antigen Test Kit");
        put("1773", "The SARS-CoV-2 Antigen Assay Kit (Immunochromatography)");
        put("2090", "SARS-CoV-2 Antigen Rapid Test Kit");
        put("1763", "COVID-19 Antigen Rapid Test Kit (Colloidal Gold)");
        put("1278", "Rapid SARS-CoV-2 Antigen Test Card");
        put("1456", "SARS-CoV-2 Antigen Rapid Test");
        put("1884", "SARS-CoV-2 Antigen Rapid Test (Colloidal Gold)");
        put("1296", "AndLucky COVID-19 Antigen Rapid Test");
        put("1295", "reOpenTest COVID-19 Antigen Rapid Test");
        put("1343", "Coronavirus Ag Rapid Test Cassette (Swab)");
        put("1957", "COVID-19 Antigen Detection Kit (Colloidal Gold)");
    }};
}
