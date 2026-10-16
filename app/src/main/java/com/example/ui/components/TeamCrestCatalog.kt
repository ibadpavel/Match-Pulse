package com.example.ui.components

import androidx.compose.ui.graphics.Color

/**
 * Статический каталог эмблем: стабильный [key] → уникальный crest URL + алиасы.
 * Матчинг exact-first, затем longest alias с границами слова (не substring внутри слова).
 */
data class TeamCrestEntry(
    val key: String,
    val aliases: List<String>,
    val crestUrl: String,
    val primaryColor: Color = Color(0xFF1A2B4C),
    val secondaryColor: Color = Color(0xFF00D2C4)
)

object TeamCrestCatalog {

    private const val FD = "https://crests.football-data.org"
    private const val JD = "https://cdn.jsdelivr.net/gh/luukhopman/football-logos@master"
    /** TheSportsDB badge CDN — fallback, когда FD/jsDelivr недоступны или ID неверны. */
    private const val TSDB = "https://r2.thesportsdb.com/images/media/team/badge"

    /** Все записи каталога. URL эмблем обязаны быть уникальны между разными key. */
    val entries: List<TeamCrestEntry> = listOf(
        // Испания
        entry("real_madrid", listOf("real madrid", "реал мадрид"), fd(86), 0xFF1E2A5E, 0xFFD4AF37),
        entry("barcelona", listOf("barcelona", "барселона"), fd(81), 0xFF004D98, 0xFFA50044),
        entry("atletico_madrid", listOf("atletico", "atleti", "атлетико", "atlet"), fd(78), 0xFFCB3524, 0xFF272E61),
        entry("athletic_bilbao", listOf("athletic", "bilbao", "бильбао"), fd(77)),
        entry("real_sociedad", listOf("sociedad", "сосьедад"), fd(92)),
        entry("villarreal", listOf("villarreal", "вильярреал"), fd(94)),
        entry("girona", listOf("girona", "жирона"), fd(298)),
        entry("celta", listOf("celta", "сельта"), fd(558)),
        entry("betis", listOf("betis", "бетис"), fd(90)),
        entry("sevilla", listOf("sevilla", "севилья"), fd(559)),

        // Англия
        entry("man_city", listOf("manchester city", "манчестер сити", "man city"), fd(65), 0xFF6CABDD, 0xFF1C2C5B),
        entry("man_united", listOf("manchester united", "манчестер юнайтед", "man utd"), fd(66)),
        entry("liverpool", listOf("liverpool", "ливерпуль"), fd(64), 0xFFC8102E, 0xFF00B2A9),
        entry("arsenal", listOf("arsenal", "арсенал"), fd(57), 0xFFDB0007, 0xFF023474),
        entry("aston_villa", listOf("aston villa", "астон вилла"), fd(58)),
        entry("chelsea", listOf("chelsea", "челси"), fd(61), 0xFF034694, 0xFFDBA111),
        entry("tottenham", listOf("tottenham", "тоттенхэм", "spurs"), fd(73)),
        entry(
            "crystal_palace",
            listOf("crystal palace", "palace", "пэлас"),
            jd("logos/England%20-%20Premier%20League/Crystal%20Palace.png")
        ),
        entry("bournemouth", listOf("bournemouth", "борнмут"), fd(1044)),
        entry("sunderland", listOf("sunderland", "сандерленд"), fd(71)),

        // Германия — Bayern aliases BEFORE Bayer ("байерн" vs "байер")
        entry("bayern", listOf("bayern", "байерн", "бавария", "bayern munchen", "bayern munich"), fd(5), 0xFFDC052D, 0xFF0066B2),
        entry("dortmund", listOf("dortmund", "дортмунд", "bvb", "b. dortmund"), fd(4), 0xFFFDE100, 0xFF111111),
        entry("leverkusen", listOf("leverkusen", "леверкузен", "байер", "bayer"), fd(3), 0xFFE32221, 0xFF111111),
        entry("leipzig", listOf("leipzig", "лейпциг"), fd(721)),
        entry("stuttgart", listOf("stuttgart", "штутгарт"), fd(10)),
        entry("frankfurt", listOf("frankfurt", "франкфурт", "айнтрахт"), fd(19)),
        entry(
            "hoffenheim",
            listOf("hoffenheim", "хоффенхайм"),
            jd("logos/Germany%20-%20Bundesliga/TSG%201899%20Hoffenheim.png")
        ),

        // Италия
        entry("inter", listOf("internazionale", "inter milan", "inter", "интер"), fd(108), 0xFF001489, 0xFF111111),
        entry("juventus", listOf("juventus", "ювентус"), fd(109), 0xFF222222, 0xFF888888),
        entry("ac_milan", listOf("ac milan", "milan", "милан"), fd(98), 0xFFFB090B, 0xFF111111),
        entry("atalanta", listOf("atalanta", "аталанта"), fd(102)),
        entry("bologna", listOf("bologna", "болонья"), fd(103)),
        entry("roma", listOf("as roma", "roma", "рома"), fd(100), 0xFF8E1F2F, 0xFFF0BC42),
        entry("lazio", listOf("lazio", "лацио"), fd(110), 0xFF87D8F7, 0xFFFFFFFF),
        entry("como", listOf("como", "комо", "como 1907"), fd(7397)),
        entry("napoli", listOf("napoli", "наполи"), fd(113)),
        entry("fiorentina", listOf("fiorentina", "фиорентина"), fd(99)),

        // Франция
        entry("psg", listOf("paris saint-germain", "paris", "psg", "псж"), fd(524), 0xFF004170, 0xFFDA291C),
        entry("monaco", listOf("monaco", "монако"), fd(548), 0xFFE2001A, 0xFFD4AF37),
        entry("lille", listOf("lille", "лилль"), fd(521)),
        entry("brest", listOf("brest", "брест"), fd(512)),
        entry("lyon", listOf("lyon", "лион"), fd(523)),
        entry("nice", listOf("nice", "ницца"), fd(522)),
        entry("rennes", listOf("rennes", "ренн"), fd(529)),
        entry("marseille", listOf("marseille", "марсель"), fd(516)),
        entry("lens", listOf("lens", "ланс"), fd(546)),

        // Португалия
        entry("sporting_cp", listOf("sporting cp", "sporting", "спортинг"), fd(498), 0xFF008057, 0xFF111111),
        entry("benfica", listOf("benfica", "бенфика"), fd(1903), 0xFFE8112D, 0xFFFFFFFF),
        entry("porto", listOf("porto", "порту"), fd(503), 0xFF0038A8, 0xFFFFFFFF),
        entry("braga", listOf("braga", "брага"), fd(5613)),
        entry("torreense", listOf("torreense", "торренсе"), tsdb("byn6c21659694982.png")),

        // Нидерланды
        entry("psv", listOf("psv", "псв"), fd(674)),
        entry("feyenoord", listOf("feyenoord", "фейеноорд"), fd(675)),
        entry("ajax", listOf("ajax", "аякс"), fd(678), 0xFFD2122E, 0xFFFFFFFF),
        entry("twente", listOf("twente", "твенте"), fd(666)),
        entry(
            "az_alkmaar",
            listOf("az alkmaar", "алкмаар"),
            jd("logos/Netherlands%20-%20Eredivisie/AZ%20Alkmaar.png")
        ),
        entry(
            "nec",
            listOf("n.e.c", "n.e.c.", "nijmegen", "nec"),
            jd("logos/Netherlands%20-%20Eredivisie/NEC%20Nijmegen.png")
        ),

        // Шотландия
        entry("celtic", listOf("celtic", "селтик"), fd(734), 0xFF018749, 0xFFFFFFFF),
        entry("rangers", listOf("rangers", "рейнджерс"), fd(732)),

        // Австрия / Швейцария
        entry(
            "salzburg",
            listOf("salzburg", "зальцбург", "red bull salzburg"),
            jd("logos/Austria%20-%20Bundesliga/Red%20Bull%20Salzburg.png")
        ),
        entry(
            "sturm",
            listOf("sturm graz", "sturm", "штурм"),
            jd("logos/Austria%20-%20Bundesliga/SK%20Sturm%20Graz.png")
        ),
        entry("lask", listOf("lask", "ласк"), fd(2016), 0xFF111111, 0xFFFFFFFF),
        entry("young_boys", listOf("young boys", "янг бойз"), fd(1871)),

        // Украина
        entry("shakhtar", listOf("shakhtar", "шахтер", "шахтар"), fd(1887), 0xFFFF5900, 0xFF111111),
        entry("dynamo_kyiv", listOf("dynamo kyiv", "dinamo kyiv", "kyiv", "киев", "динамо киев"), fd(1873)),

        // Балканы
        entry(
            "dinamo_zagreb",
            listOf("dinamo zagreb", "gnk dinamo", "загреб", "динамо загреб", "dinamo"),
            jd("logos/Croatia%20-%20SuperSport%20HNL/GNK%20Dinamo%20Zagreb.png")
        ),
        entry(
            "crvena_zvezda",
            listOf("crvena zvezda", "zvezda", "црвена", "звезда"),
            tsdb("osgmbz1781157114.png")
        ),
        // fd(1890) — placeholder, не герб Celje
        entry("celje", listOf("celje", "целе", "nk celje"), tsdb("amvs371681106952.png")),

        // Чехия / Словакия / Венгрия / Польша
        entry(
            "sparta_prague",
            listOf("sparta praha", "sparta prague", "sparta", "спарта"),
            jd("logos/Czech%20Republic%20-%20Chance%20Liga/AC%20Sparta%20Prague.png")
        ),
        entry("slavia_prague", listOf("slavia praha", "slavia", "славия"), fd(930)),
        entry(
            "viktoria_plzen",
            listOf("viktoria plzen", "plzen", "пльзень", "viktoria"),
            jd("logos/Czech%20Republic%20-%20Chance%20Liga/FC%20Viktoria%20Plzen.png")
        ),
        entry("slovan_bratislava", listOf("slovan bratislava", "bratislava", "s. bratislava", "слован"), fd(7509)),
        entry(
            "ferencvaros",
            listOf("ferencvaros", "ференцварош"),
            tsdb("wk17od1688115265.png"),
            0xFF008000,
            0xFFFFFFFF
        ),
        entry(
            "jagiellonia",
            listOf("jagiellonia", "ягеллония"),
            jd("logos/Poland%20-%20PKO%20BP%20Ekstraklasa/Jagiellonia%20Bialystok.png")
        ),
        entry(
            "lech_poznan",
            listOf("lech poznan", "lech", "познань"),
            jd("logos/Poland%20-%20PKO%20BP%20Ekstraklasa/Lech%20Poznan.png")
        ),

        // Скандинавия
        entry("bodo_glimt", listOf("bodo/glimt", "bodo", "буде", "будё", "glimt"), fd(5721), 0xFFFFCC00, 0xFF111111),
        entry("viking", listOf("viking", "викинг"), fd(5720)),
        entry("malmo", listOf("malmo", "мальме", "мальмё"), fd(1892)),
        entry("elfsborg", listOf("elfsborg", "эльфсборг"), fd(1893)),
        entry(
            "lillestrom",
            listOf("lillestrom", "лиллестрем"),
            jd("logos/Norway%20-%20Eliteserien/Lillestr%C3%B8m%20SK.png")
        ),

        // Турция
        entry("galatasaray", listOf("galatasaray", "галатасарай"), fd(610), 0xFFA90432, 0xFFFDB913),
        entry("fenerbahce", listOf("fenerbahce", "fenerbah", "фенербахче"), fd(613), 0xFF002D72, 0xFFFFED00),
        entry("besiktas", listOf("besiktas", "бешикташ"), fd(554), 0xFF111111, 0xFFFFFFFF),

        // Греция / Кипр
        entry(
            "olympiacos",
            listOf("olympiacos", "олимпиакос"),
            jd("logos/Greece%20-%20Super%20League%201/Olympiacos%20Piraeus.png")
        ),
        // fd(656) → 404
        entry(
            "paok",
            listOf("paok", "паок"),
            jd("logos/Greece%20-%20Super%20League%201/PAOK%20Thessaloniki.png")
        ),
        entry("aek_athens", listOf("aek athens", "aek", "аек"), fd(1899), 0xFFFFCC00, 0xFF111111),
        // fd(1894) — placeholder; badge Omonia Nicosia (не Omonia 29M)
        entry("omonia", listOf("omonia", "омония", "omonia nicosia"), tsdb("nmd7vt1779579017.png")),
        entry(
            "ofi_crete",
            listOf("ofi crete", "ofi", "офи"),
            jd("logos/Greece%20-%20Super%20League%201/OFI%20Crete.png")
        ),

        // Бельгия
        entry("club_brugge", listOf("club brugge", "brugge", "брюгге"), fd(851), 0xFF0066B2, 0xFF111111),
        // fd(688) → 404 — критично для ЛЕ (Union SG)
        entry(
            "union_sg",
            listOf("union sg", "union saint-gilloise", "юнион", "saint-gilloise"),
            jd("logos/Belgium%20-%20Jupiler%20Pro%20League/Union%20Saint-Gilloise.png")
        ),
        entry(
            "anderlecht",
            listOf("anderlecht", "rsc anderlecht", "андерлехт"),
            jd("logos/Belgium%20-%20Jupiler%20Pro%20League/RSC%20Anderlecht.png")
        ),

        // Прочие еврокубки — уникальные URL (раньше были коллизии ID)
        entry(
            "maccabi_tel_aviv",
            listOf("maccabi tel aviv", "maccabi", "маккаби"),
            jd("logos/Israel%20-%20Ligat%20ha%27Al/Maccabi%20Tel%20Aviv.png")
        ),
        entry(
            "hapoel_beer_sheva",
            listOf("hapoel beer sheva", "h. beer-sheva", "beer-sheva", "beer sheva", "беэр-шева"),
            jd("logos/Israel%20-%20Ligat%20ha%27Al/Hapoel%20Beer%20Sheva.png")
        ),
        entry(
            "levski_sofia",
            listOf("levski sofia", "levski", "левски"),
            jd("logos/Bulgaria%20-%20efbet%20Liga/Levski%20Sofia.png")
        ),
        entry(
            "ludogorets",
            listOf("ludogorets", "лудогорец"),
            jd("logos/Bulgaria%20-%20efbet%20Liga/Ludogorets%20Razgrad.png")
        ),
        entry("sabah", listOf("sabah", "сабах"), fd(10233)),
        entry("fcsb", listOf("fcsb", "стеауа", "стяуа"), tsdb("123g021759420850.png")),
        // ЛЕ 2026: Ararat-Armenia отсутствует в FD/jsDelivr
        entry(
            "ararat_armenia",
            listOf("ararat-armenia", "ararat armenia", "ararat", "арарат"),
            tsdb("g396se1596470244.png")
        )
    )

    private val exactAliasIndex: Map<String, TeamCrestEntry>
    private val aliasesByLengthDesc: List<Pair<String, TeamCrestEntry>>

    init {
        val urlOwners = mutableMapOf<String, String>()
        val aliasOwners = mutableMapOf<String, String>()
        for (entry in entries) {
            val prevKey = urlOwners.put(entry.crestUrl, entry.key)
            require(prevKey == null) {
                "Duplicate crest URL for keys '$prevKey' and '${entry.key}': ${entry.crestUrl}"
            }
            for (alias in entry.aliases) {
                val normalized = TeamNameNormalizer.clean(alias)
                require(normalized.isNotBlank()) { "Blank alias for ${entry.key}" }
                val prev = aliasOwners.put(normalized, entry.key)
                // Один и тот же клуб может дать один normalized-ключ из разных сырых алиасов
                // (например "fc porto" и "porto" → "porto" после strip префиксов).
                require(prev == null || prev == entry.key) {
                    "Duplicate alias '$normalized' for keys '$prev' and '${entry.key}'"
                }
            }
        }

        exactAliasIndex = buildMap {
            for (club in this@TeamCrestCatalog.entries) {
                for (alias in club.aliases) {
                    put(TeamNameNormalizer.clean(alias), club)
                }
            }
        }

        aliasesByLengthDesc = exactAliasIndex.entries
            .map { it.key to it.value }
            .sortedByDescending { it.first.length }
    }

    fun resolve(cleanName: String): TeamCrestEntry? {
        if (cleanName.isBlank()) return null
        exactAliasIndex[cleanName]?.let { return it }

        for ((alias, entry) in aliasesByLengthDesc) {
            if (matchesAlias(cleanName, alias)) return entry
        }
        return null
    }

    fun resolveRaw(rawTeamName: String): TeamCrestEntry? =
        resolve(TeamNameNormalizer.clean(rawTeamName))

    /**
     * Alias совпадает как целый токен/фраза, не как подстрока внутри слова.
     * "байер" не матчит "байерн"; "lech" не матчит "leicester".
     */
    internal fun matchesAlias(clean: String, alias: String): Boolean {
        if (clean == alias) return true
        if (alias.length < 2) return false
        val pattern = Regex(
            "(?<![\\p{L}\\p{N}])${Regex.escape(alias)}(?![\\p{L}\\p{N}])",
            RegexOption.IGNORE_CASE
        )
        return pattern.containsMatchIn(clean)
    }

    fun allCrestUrls(): Set<String> = entries.map { it.crestUrl }.toSet()

    private fun entry(
        key: String,
        aliases: List<String>,
        crestUrl: String,
        primary: Long = 0xFF1A2B4C,
        secondary: Long = 0xFF00D2C4
    ) = TeamCrestEntry(
        key = key,
        aliases = aliases,
        crestUrl = crestUrl,
        primaryColor = Color(primary),
        secondaryColor = Color(secondary)
    )

    private fun fd(id: Int) = "$FD/$id.png"
    private fun jd(path: String) = "$JD/$path"
    private fun tsdb(file: String) = "$TSDB/$file"
}
