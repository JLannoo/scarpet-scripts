__config() -> {
    'commands' -> {
        'count' -> 'count_mobs',
    },
};

count_mobs() -> (
    monster_count = {};

    monster_list = entity_list('monster');

    for(monster_list, monster_count:str(_) += 1);

    for(sort_key(keys(monster_count), monster_count:_),
        print(format('be ' + _) + ': ' + monster_count:_)
    );
);