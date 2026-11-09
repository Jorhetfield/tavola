package com.tavola.domain.model

/**
 * Guesses the category of a shopping item from what is typed, so the person almost never has to pick one. It knows the common
 * Spanish and English names of food and household things; anything else has no suggestion and the caller falls back to
 * [ShoppingCategory.Other]. Words are compared without accents, case or a plural ending, so "Tomates" and "tómate" both find "tomate".
 */
object ShoppingCategorySuggester {
    private val knownWords: Map<String, ShoppingCategory> = buildMap {
        fun add(category: ShoppingCategory, words: String) = words.split(' ').forEach { word -> put(word, category) }
        add(
            ShoppingCategory.FruitAndVegetables,
            "tomate cebolla ajo patata papa zanahoria lechuga pepino pimiento berenjena calabacin espinaca brocoli coliflor " +
                "champinon seta limon naranja mandarina manzana platano banana pera fresa uva melon sandia melocoton aguacate fruta " +
                "verdura ensalada perejil albahaca puerro calabaza apio judia guisante " +
                "tomato onion garlic potato carrot lettuce cucumber pepper eggplant zucchini spinach broccoli cauliflower mushroom " +
                "lemon orange apple pear strawberry grape watermelon peach avocado fruit vegetable parsley basil leek pumpkin celery bean pea",
        )
        add(
            ShoppingCategory.MeatAndFish,
            "pollo pavo ternera cerdo cordero carne jamon chorizo salchicha bacon panceta hamburguesa pescado merluza salmon atun " +
                "bacalao gamba langostino mejillon calamar pulpo sardina anchoa " +
                "chicken turkey beef pork lamb meat ham sausage fish hake tuna cod shrimp prawn mussel squid octopus sardine anchovy",
        )
        add(
            ShoppingCategory.DairyAndEggs,
            "leche queso yogur yogurt mantequilla nata huevo requeson mozzarella feta parmesano kefir cuajada " +
                "milk cheese yoghurt butter cream egg ricotta parmesan",
        )
        add(
            ShoppingCategory.Pantry,
            "arroz pasta espagueti macarron harina azucar sal aceite vinagre lenteja garbanzo alubia cereal cafe cacao miel mermelada " +
                "conserva salsa especia pimenton oregano comino azafran legumbre fideo tallarin galleta chocolate almendra nuez " +
                "rice spaghetti flour sugar salt oil vinegar lentil chickpea coffee cocoa honey jam sauce spice paprika cumin saffron " +
                "cookie almond walnut nut",
        )
        add(
            ShoppingCategory.Bakery,
            "pan baguette barra tostada bollo croissant magdalena bizcocho empanada masa bread bun muffin cake toast dough",
        )
        add(ShoppingCategory.Frozen, "congelado congelada helado hielo frozen ice")
        add(
            ShoppingCategory.Drinks,
            "agua cerveza vino zumo refresco cola gaseosa licor whisky ginebra sidra tonica water beer wine juice soda lemonade liquor gin cider tonic",
        )
        add(
            ShoppingCategory.Household,
            "papel detergente lejia jabon champu gel suavizante fregasuelos bayeta estropajo bolsa basura servilleta dentifrico cepillo " +
                "esponja ambientador pila bombilla aluminio detergent bleach soap shampoo softener sponge napkin toothpaste toothbrush " +
                "battery bulb foil tissue",
        )
    }

    /** Names whose first word would point to the wrong category ("pasta de dientes" is not pantry). They are checked before the words. */
    private val phrases: Map<String, ShoppingCategory> = mapOf(
        "pasta de dientes" to ShoppingCategory.Household,
        "pasta dental" to ShoppingCategory.Household,
        "agua oxigenada" to ShoppingCategory.Household,
        "agua de colonia" to ShoppingCategory.Household,
    )

    /**
     * The category [name] most likely belongs to: a known phrase if the name has one, otherwise the first word that is known.
     * `null` if nothing is known.
     */
    fun suggest(name: String): ShoppingCategory? {
        val words = ShoppingNames.words(name)
        val text = words.joinToString(separator = " ", prefix = " ", postfix = " ")
        return phrases.entries.firstOrNull { (phrase, _) -> text.contains(" $phrase ") }?.value
            ?: words.firstNotNullOfOrNull { word -> word.known() }
    }

    private fun String.known(): ShoppingCategory? = knownWords[this] ?: knownWords[removeSuffix("s")] ?: knownWords[removeSuffix("es")]
}
