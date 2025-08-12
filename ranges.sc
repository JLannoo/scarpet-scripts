global_ranges = {
    'spawn' -> {
        'radius' -> 24,
        'enabled' -> false,
    },
    'can_despawn' -> {
        'radius' -> 32,
        'enabled' -> false,
    },
    'despawn' -> {
        'radius' -> 127,
        'enabled' -> false,
    },
};

global_follow = false;
global_frozen_position = pos(player());
global_update_rate = 10;

__config() -> {
    'commands' -> {
        'toggle <range_type>' -> 'toggle_range',
        'clear' -> 'clear_ranges',
        'freeze' -> 'freeze_ranges',
        'follow' -> 'unfreeze_ranges',
        'rate <ticks>' -> 'set_update_rate',
    },
    'arguments' -> {
        'range_type' -> {
            'type' -> 'term',
            'options' -> keys(global_ranges),
        },
        'ticks' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 20,
            'suggest' -> [1, 10, 20],
        },
    }
};

toggle_range(range_type) -> (
    (global_ranges:range_type):'enabled' = !(global_ranges:range_type):'enabled';
    print('Range ' + range_type + ' is now ' + if ((global_ranges:range_type):'enabled', 'enabled', 'disabled'));

    // Check if any ranges are still enabled
    has_enabled = false;
    for(keys(global_ranges),
        if((global_ranges:_):'enabled',
            has_enabled = true;
        )
    );

    if(has_enabled, schedule(global_update_rate, '__draw_ranges'));
);

clear_ranges() -> (
    for(keys(global_ranges), (global_ranges:_):'enabled' = false);
    
    global_follow = false;
    global_frozen_position = pos(player());
    print('All ranges cleared and disabled.');
);

freeze_ranges() -> (
    global_follow = false;
    global_frozen_position = pos(player());
);

unfreeze_ranges() -> (
    global_follow = true;
);

set_update_rate(ticks) -> (
    global_update_rate = ticks;
    print('Update rate set to ' + ticks + ' ticks.');
);

__draw_ranges() -> (
    // Check if any ranges are enabled before continuing
    has_enabled = false;
    for(keys(global_ranges),
        if((global_ranges:_):'enabled',
            has_enabled = true;
            shape_config = {
                'center' -> if(global_follow, [0,0,0], global_frozen_position),
                'radius' -> (global_ranges:_):'radius',
                'color' -> 0xffffffff,
                'line' -> 1,
                'fill' -> 100,
            };

            if(global_follow, shape_config:'follow' = player());

            draw_shape('sphere', global_update_rate, shape_config);
        )
    );

    // Only reschedule if we have enabled ranges and global_drawing is still true
    if(has_enabled, schedule(global_update_rate, '__draw_ranges'));
)