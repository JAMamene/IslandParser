    -------------------------------------------------------------------------------------------
    Puskaric Jean-Adam                                                         Guillaume André

                           Modélisation et analyse des données Island

        Exécution du programme

    Nous avons utilisé Maven pour gérer nos dépendances. Le convertisseur JSON -> XML et
    l'analyseur XML peuvent être exécutés avec la commande suivante:

        mvn exec:java -Dexec.args="exemple\Explorer_iadc.json"

    Le programme prend comme unique paramètre le fichier contenant la trace JSON. La commande
    doit être exécutée au même niveau que islands.dtd. Le résultat de la conversion est
    enregistré dans le fichier output.xml, et les statistiques de la trace XML sont affichées
    en console.


        Modélisation de la trace JSON sous forme XML

    La trace JSON par l'éxécution du projet Islands consistent concrètement en une array
    principale, contenat trois types d'objets distincts:
        - L'objet unique contenant l'initialisation du contexte;
        - Un objet correspondant à une action d'un bot explorateur;
        - Un objet correspondant à la réponse du simulateur à cette action.

    Pour notre représentation XML, nous avons choisi de fusionner les paires d'objets
    Action/Réponse sous un unique élément <turn>. Chacun de ces éléments contient un élément
    <action> et un élément <answer>, contenant les informations des objets JSON originaux.
    L'ensemble de ces <turn> sont eux-mêmes placés dans un élément <actions>, afin de les
    dissocier des données d'initialisation placées dans une balise <context> au début du
    document.

    On gagne ainsi beacoup de clarté par rapport au JSON original. Cela permet également de
    se débarasser des données "part" et "meth" de chacun des JSONObjects du JSONArray
    principal. En ce qui concerne la donnée "time" correspondant au temps d'éxécution pour
    chaque tour, nous avons décidé de ne pas l'inclure dans notre représentation XML. Etant
    donné qu'elle dépend essentiellement de la machine sur laquelle le simulateur tourne,
    elle ne présente que peu d'interêt pour l'observation du comportement d'un bot.

    En ce qui concerne la structure interne des actions et des réponses, nous sommes restés
    proches de la structure JSON originale, en passant certains éléments en attribut lorsque
    nous avons jugé cela approprié (nom des actions, statut de la réponse, etc.).

    Nous avons également réduit le nombre d'éléments existants par rapport à la structure
    originale. Par exemple, plutôt que d'avoir plusieurs modèles différents pour les resources
    comme dans le JSON original (paramètres de TRANSFORM, EXPLOIT, réponse de SCOUT, EXPLORE et
    TRANSFORM), il existe un unique élément <resource> dans notre XML. Cet élément peut ensuite
    posséder plusieurs éléments enfants différents selon le contexte où il est utilisé.

    Enfin, le modèle de l'action GLIMPSE est simplifiée dans notre XML: les 4 éléments
    correspondant aux données sur chacune des cases observées partagent la même structure, ce
    qui n'est pas le cas dans le JSON original.


        Analyse du XML produit

    En ce qui concerne l'analyse du document xml obtenu, nous avons utilisé la bibliothèque
    org.w3c. C'est la classe XMLAnalyzer qui s'en charge.
    Pour une action en particulier, ou toutes les actions, on peut:
        - Obtenir le nombre total de ces actions
        - Obtenir la somme du coût
        - Obtenir le coût minimum et maximum
        - Obtenir la moyenne du coût
    Tout cela grâce aux streams de java 8.

    On peut également avoir une map des ressources obtenus exactes, autrement dit on a la
    quantité de chaque ressource exploitée et transformée, tout en prenant compte le fait
    qu'une ressource n'existe plus une fois utilisée pour en fabriquer une autre.


        Style du XML

    Pour ce qui est du style du document XML obtenu, nous avons utilisé en particulier les
    pseudo classes CSS, before et after afin de pouvoir rajouter du texte et faire apparaitre
    les attributs à l'écran. De plus, nous avons choisi une présentation plus matérielle grâce
    à l'utilisation de display: block pour certains éléments, ainsi que des effets d'ombres et
    de background.
    Enfin nous avons rajouté une petite surcouche javascript totalement optionnelle, qui se
    contente de rendre les actions clickable pour avoir plus d'information dessus et
    d'afficher quelques statistiques calculées à partir du contexte.