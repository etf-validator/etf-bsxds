(:~
 : ----------------------------------------------------------------
 : ETF XQuery data storage Function Library
 : ----------------------------------------------------------------
 :)
module namespace  etfxdb = "http://interactive_instruments.de/etf/etfxdb" ;

declare default element namespace "http://www.interactive-instruments.de/etf/2.0";
declare namespace etf = "http://www.interactive-instruments.de/etf/2.0";

(:~
 : ----------------------------------------------------------------
 : get-replacedByRec
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-replacedByRec($dbs as node()*, $levelOfDetail as xs:string, $item as node()*) {
    if ($levelOfDetail = 'HISTORY')
    then
        etfxdb:get-replacedByRec($dbs, $item)
    else
        ()
};

declare function etfxdb:get-replacedByRec($dbs as node()*, $item as node()*) {
    let $replacedBy := $dbs[@id = $item/etf:replacedBy[1]/@ref]
    return
        if (empty($replacedBy))
        then
            ()
        else
            ($replacedBy, etfxdb:get-replacedByRec($dbs, $replacedBy))
};

(:~
 : ----------------------------------------------------------------
 : get-all
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-all($items as node()*, $levelOfDetail as xs:string, $offset as xs:integer, $limit as xs:integer) {
    (
        for $item in $items
        where
            if (not($levelOfDetail = 'HISTORY'))
            then
                not(exists($item/etf:replacedBy[1]))
            else
                true()
        order by $item/etf:label ascending
        return
            $item )[position() > $offset and position() <= $offset + $limit]
};

declare function etfxdb:get-all($items as node()*, $offset as xs:integer, $limit as xs:integer) {
    (
        for $item in $items
        order by $item/etf:label ascending
        return
            $item )[position() > $offset and position() <= $offset + $limit]
};

(:~
 : ----------------------------------------------------------------
 : get-parent
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-parentRec($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    let $parent := $dbs[@id = $items/etf:parent[1]/@ref]
    return
    (: todo :)
        if (empty($parent))
        then
            ()
        else
            ($parent, etfxdb:get-parentRec($dbs, $parent))
};

declare function etfxdb:get-parentRec($dbs as node()*, $items as node()*) {
    let $parent := $dbs[@id = $items/etf:parent[1]/@ref]
    return
    (: todo :)
        if (empty($parent))
        then
            ()
        else
            ($parent, etfxdb:get-parentRec($dbs, $parent))
};

(:~
 : ----------------------------------------------------------------
 : get-testObjectTypes
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testObjectTypes($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED')
    then
        let $testObjectTypes := $dbs[@id = $items/etf:testObjectType[1]/@ref]
        return
            if (empty($testObjectTypes))
            then
                ()
            else
                ($testObjectTypes, etfxdb:get-testObjectTypesRec($dbs, $testObjectTypes))
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-testItemTypes
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testItemTypes($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED')
    then
        $dbs[@id = $items/etf:type[1]/@ref]
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-testObjects
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testObjects($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED')
    then
        $dbs[@id = $items/etf:testObject[1]/@ref]
    else
        ()
};

declare function etfxdb:get-testObjectTypesRec($dbs as node()*, $testObjectTypes as node()*) {
    let $testObjectTypes := $dbs[@id = $testObjectTypes/etf:subTypes[1]/etf:testObjectType/@ref]
    return
        if (empty($testObjectTypes))
        then
            ()
        else
            ($testObjectTypes, etfxdb:get-testObjectTypesRec($dbs, $testObjectTypes))
};

(:~
 : ----------------------------------------------------------------
 : get-executableTestSuites
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-executableTestSuites($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED')
    then
        $dbs[@id = $items/etf:resultedFrom[1]/@ref or @id = $items/etf:executableTestSuite[1]/@ref]
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-testTaskResults
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testTaskResults($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED')
    then
        $dbs[@id = $items/etf:testTaskResult[1]/@ref]
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-tags
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-tags($dbs as node()*, $levelOfDetail as xs:string, $item as node()*) {
    if ($levelOfDetail = 'DETAILED')
    then
        let $tags := $dbs[@id = $item/etf:tags[1]/etf:tag/@ref]
        return
            if (empty($tags))
            then
                ()
            else
                ($tags, etfxdb:get-replacedByRec($dbs, $tags))
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-translationTemplateBundels
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-translationTemplateBundles($dbs as node()*, $levelOfDetail as xs:string, $item as node()*) {
    if ($levelOfDetail = 'DETAILED')
    then
        $dbs[@id = $item/etf:translationTemplateBundle[1]/@ref]
    else
        ()
};