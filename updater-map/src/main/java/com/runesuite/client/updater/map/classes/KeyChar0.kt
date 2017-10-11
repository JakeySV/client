package com.runesuite.client.updater.map.classes

import com.runesuite.mapper.IdentityMapper
import com.runesuite.mapper.annotations.DependsOn
import com.runesuite.mapper.annotations.SinceVersion
import com.runesuite.mapper.extensions.and
import com.runesuite.mapper.extensions.predicateOf
import com.runesuite.mapper.tree.Class2

@SinceVersion(141)
@DependsOn(KeyChar1::class, Enumerated::class)
class KeyChar0 : IdentityMapper.Class() {
    override val predicate = predicateOf<Class2> { klass<Enumerated>() != it }
            .and { klass<KeyChar1>().interfaces.contains(it.type) }
}