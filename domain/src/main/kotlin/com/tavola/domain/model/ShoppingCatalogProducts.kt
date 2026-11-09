package com.tavola.domain.model

// The products of the built-in catalog. A row is `emoji|spanish|english` and then any number of `|alias`. The id of a product is its
// English name as a key, so it has to stay the same: the image of the product is found by it. A plural needs no alias, because names
// are compared ignoring a plural ending ("tomates" finds "tomate"). The Noto Emoji of each row are the images the app has to bundle.

private fun products(category: ShoppingCategory, vararg rows: String): List<CatalogProduct> = rows.map { row ->
    val parts = row.split('|')
    CatalogProduct(
        id = ShoppingNames.key(parts[2]).replace(' ', '-'),
        emoji = parts[0],
        category = category,
        spanish = parts[1],
        english = parts[2],
        aliases = parts.drop(3),
    )
}

private val fruitAndVegetables = products(
    ShoppingCategory.FruitAndVegetables,
    "🍅|tomate|tomato",
    "🥕|zanahoria|carrot",
    "🧅|cebolla|onion",
    "🧄|ajo|garlic",
    "🥔|patata|potato|papa",
    "🍠|boniato|sweet potato|batata",
    "🥬|lechuga|lettuce",
    "🥬|espinacas|spinach",
    "🥬|apio|celery",
    "🥗|ensalada|salad",
    "🥒|pepino|cucumber",
    "🥒|calabacín|zucchini|courgette",
    "🫑|pimiento|pepper|bell pepper",
    "🍆|berenjena|eggplant|aubergine",
    "🥦|brócoli|broccoli",
    "🍄|champiñones|mushroom|seta",
    "🎃|calabaza|pumpkin",
    "🌽|maíz|corn|mazorca",
    "🫛|guisantes|peas",
    "🫛|judías verdes|green beans",
    "🌶️|guindilla|chili|chile",
    "🫚|jengibre|ginger",
    "🌿|perejil|parsley",
    "🌿|albahaca|basil",
    "🍋|limón|lemon",
    "🍊|naranja|orange",
    "🍊|mandarina|mandarin|clementina",
    "🍎|manzana|apple",
    "🍌|plátano|banana",
    "🍐|pera|pear",
    "🍓|fresas|strawberry",
    "🍇|uvas|grape",
    "🍉|sandía|watermelon",
    "🍈|melón|melon",
    "🍑|melocotón|peach|durazno",
    "🍍|piña|pineapple",
    "🥑|aguacate|avocado",
    "🥭|mango|mango",
    "🥝|kiwi|kiwi",
    "🍒|cerezas|cherry",
    "🫐|arándanos|blueberry",
)

private val meatAndFish = products(
    ShoppingCategory.MeatAndFish,
    "🍗|pollo|chicken",
    "🥩|ternera|beef",
    "🥩|carne picada|ground meat|mince",
    "🍖|cerdo|pork",
    "🍖|cordero|lamb",
    "🦃|pavo|turkey",
    "🥓|bacon|bacon|panceta",
    "🍖|jamón|ham",
    "🌭|chorizo|chorizo",
    "🌭|salchichas|sausage",
    "🍔|hamburguesas|burger",
    "🐟|pescado|fish",
    "🐟|merluza|hake",
    "🐟|salmón|salmon",
    "🐟|atún|tuna",
    "🦐|gambas|shrimp|langostino|prawn",
    "🦑|calamares|squid",
    "🐙|pulpo|octopus",
    "🦪|mejillones|mussel",
)

private val dairyAndEggs = products(
    ShoppingCategory.DairyAndEggs,
    "🥛|leche|milk",
    "🧀|queso|cheese",
    "🧀|queso rallado|grated cheese",
    "🧀|parmesano|parmesan",
    "🥚|huevos|egg",
    "🧈|mantequilla|butter",
    "🥣|yogur|yoghurt|yogurt",
    "🥛|nata|cream",
    "🍮|flan|flan|natillas",
)

private val pantry = products(
    ShoppingCategory.Pantry,
    "🍚|arroz|rice",
    "🍝|pasta|pasta",
    "🍝|espaguetis|spaghetti",
    "🫘|garbanzos|chickpea",
    "🫘|lentejas|lentil",
    "🫘|alubias|bean",
    "🫒|aceite de oliva|olive oil",
    "🌻|aceite de girasol|sunflower oil",
    "🫙|vinagre|vinegar",
    "🧂|sal|salt",
    "🧂|pimienta|black pepper",
    "🌶️|pimentón|paprika",
    "🌿|orégano|oregano",
    "🌼|azafrán|saffron",
    "🍬|azúcar|sugar",
    "🌾|harina|flour",
    "🍯|miel|honey",
    "🫙|mermelada|jam",
    "🫙|mayonesa|mayonnaise",
    "🥫|ketchup|ketchup",
    "🥫|tomate frito|tomato sauce|salsa de tomate",
    "🥫|atún en lata|canned tuna",
    "🍜|caldo|broth|sopa|soup",
    "☕|café|coffee",
    "🍵|té|tea",
    "🍫|chocolate|chocolate",
    "🍫|cacao|cocoa",
    "🍪|galletas|cookie",
    "🥣|cereales|cereal",
    "🥜|frutos secos|nuts|nuez",
    "🌰|almendras|almond",
    "🥜|cacahuetes|peanut",
    "🍿|palomitas|popcorn",
)

private val bakery = products(
    ShoppingCategory.Bakery,
    "🍞|pan|bread",
    "🍞|pan de molde|sliced bread",
    "🥖|baguette|baguette|barra",
    "🥐|croissant|croissant",
    "🫓|pan de pita|pita bread",
    "🧁|magdalenas|muffin",
    "🍰|tarta|cake|bizcocho",
)

private val frozen = products(
    ShoppingCategory.Frozen,
    "🍦|helado|ice cream",
    "🧊|hielo|ice",
    "🍕|pizza|pizza",
    "🍟|patatas fritas|fries",
    "🥦|verdura congelada|frozen vegetables",
)

private val drinks = products(
    ShoppingCategory.Drinks,
    "💧|agua|water",
    "🥤|refresco|soda|cola",
    "🥤|tónica|tonic water",
    "🧃|zumo|juice",
    "🍺|cerveza|beer",
    "🍷|vino|wine",
    "🍾|cava|sparkling wine|champán",
    "🍏|sidra|cider",
    "🍸|ginebra|gin",
    "🥃|whisky|whisky",
)

private val household = products(
    ShoppingCategory.Household,
    "🧻|papel higiénico|toilet paper",
    "🧻|papel de cocina|paper towels",
    "🧻|servilletas|napkin",
    "🧴|detergente|detergent",
    "🧴|suavizante|fabric softener",
    "🧴|lejía|bleach",
    "🫧|lavavajillas|dish soap",
    "🧹|fregasuelos|floor cleaner",
    "🧽|estropajo|scouring pad|esponja|sponge",
    "🧼|jabón|soap",
    "🧴|champú|shampoo",
    "🧴|gel de ducha|shower gel",
    "🪥|cepillo de dientes|toothbrush",
    "🦷|pasta de dientes|toothpaste",
    "🪒|cuchillas de afeitar|razor",
    "🗑️|bolsas de basura|trash bag|garbage bag",
    "🔋|pilas|battery",
    "💡|bombillas|light bulb",
)

private val other = products(
    ShoppingCategory.Other,
    "🐶|comida de perro|dog food",
    "🐱|comida de gato|cat food",
    "🐱|arena de gato|cat litter",
    "💐|flores|flowers",
    "🎁|regalo|gift",
)

/** Every product of the catalog, grouped by category in the order the categories are shown. */
internal val catalogProducts: List<CatalogProduct> =
    fruitAndVegetables + meatAndFish + dairyAndEggs + pantry + bakery + frozen + drinks + household + other
