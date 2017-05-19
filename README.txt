Puskaric Jean-Adam                  Guillaume André

// blabla sur la dtd et conversion en xml


    En ce qui concerne l'analyse du document xml obtenu, nous avons utilisé la bibliothèque org.w3c. C'est la classe
XMLAnalyzer qui s'en charge.
Pour une action en particulier ou toute les actions, on peut:
    - Obtenir le nombre total de ces actions
    - Obtenir la somme du coût
    - Obtenir le coût minimum et maximum
    - Obtenir la moyenne du coût
    Et tout cela grâce aux streams de java 8

On peut également avoir une map des ressources obtenus exactes,
    autrement dit on a la quantité de chaque ressource exploité et
    transformé, tout en prenant compte le fait qu'une ressource
    n'existe plus une fois utilisé pour en fabriquer une autre.

//blabla sur json schema


Pour ce qui est du style du document XML obtenu, nous avons utilisé
en particulier les pseudo classes CSS, before et after afin de pouvoir
rajouter du texte et faire apparaitre les attributs à l'écran, de
plus nous avons choisi une présentation plus matériel grâce à l'utilisation
de display: block pour certains éléments et d'effets d'ombres et de background
Enfin nous avons rajouté une petite surcouche javascript totalement
optionnelle qui se contente de rendre les actions clickable pour avoir
plus d'information dessus, ainsi que d'afficher quelques statistiques
calculées après le contexte.