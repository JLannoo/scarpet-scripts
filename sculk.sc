__config()->{
    'commands' -> {
        'toggle' -> 'toggle',
        'rate <ticks>' -> 'set_rate',

    },
    'libraries' -> {
        'schalk' -> 'lib/schalk/schalk.scl',
        'draw' -> 'lib/draw.scl',
    },
    'arguments' -> {
        'ticks' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 20,
            'suggest' -> [1, 10, 20],
        },
    },
};

import('schalk', 'schalk', 'schalk_rgb', 'schalk_hex', 'schalk_component', 'schalk_join', 'schalk_hover', 'schalk_url', 'schalk_suggest', 'schalk_command', 'schalk_copy');
import('draw', 'draw_line_from_to', 'draw_label_list');

global_state = {
    'enabled' -> false,
    'update_rate' -> 10,
    'trace_range' -> 5,
};

// ===== COMMANDS =====

toggle() -> (
    global_state:'enabled' = !global_state:'enabled';
    print('Sculk tracking is now ' + if(global_state:'enabled', 'enabled', 'disabled'));
    if(global_state:'enabled', schedule(0, '__draw_loop'));
);

set_rate(ticks) -> (
    global_state:'update_rate' = ticks;
    print('Update rate set to ' + ticks + ' ticks.');
);

// ===== FUNCIONALITY =====
cursor_block() -> (
    q = query(player(), 'trace', global_state:'trace_range', 'blocks');
    if(q == null, null, [q, pos(q)])
);

sculk_at_coords(coords) -> (
    block = block(coords);
    if(block != 'sculk_sensor' && block != 'sculk_shrieker',
        return(null)
    );

    [block, coords]
);

sculk_at_cursor() -> (
    block = cursor_block();
    if(block == null,
        return(null)
    );

    if(block:0 != 'sculk_sensor' && block:0 != 'sculk_shrieker',
        return(null)
    );

    return(block);
);

nearby_sculk(coords, range) -> (
    sensors = [];
    shriekers = [];
    scan(coords, [range, range, range], 
        if(_ != 'sculk_sensor' && _ != 'sculk_shrieker',
            continue();
        );

        if(_x == coords:0 && _y == coords:1 && _z == coords:2,
            continue();
        );

        if(_ == 'sculk_sensor',
            put(sensors, null, [_, [_x, _y, _z]]);
        );
        if(_ == 'sculk_shrieker',
            put(shriekers, null, [_, [_x, _y, _z]]);
        );
    );

    return({
        'sculk_sensor' -> sensors,
        'sculk_shrieker' -> shriekers,
    });
);

// ===== DRAW LOOP =====
__draw_overlay() -> (
    sculk = sculk_at_cursor();
    if(sculk == null,
        return()
    );

    nearby = nearby_sculk(sculk:1, 8);

    label_list = [
        schalk('yellow', 'Nearby:'),
        'Sensors: ' + length(nearby:'sculk_sensor'),
        schalk(if(length(nearby:'sculk_shrieker') != 0, 'red', 'gray'), 'Shriekers: ' + length(nearby:'sculk_shrieker')),
    ];

    if(length(nearby:'sculk_shrieker') != 0,
        put(label_list, 0, schalk('red', 'Warning: Nearby shriekers!'), 'extend');
    );

    draw_label_list(sculk:1, label_list, global_state:'update_rate');

    // Draw lines to all visited sculks
    if(length(nearby:'sculk_sensor') > 0,
        draw_line_from_to(sculk:0, map(nearby:'sculk_sensor', _:0), global_state:'update_rate', null, null, {
            'line' -> {
                'from' -> [0.5, 0.5, 0.5],
                'to' -> [0.5, 0.5, 0.5],
            },
        });
    );

    if(length(nearby:'sculk_shrieker') > 0,
        draw_line_from_to(sculk:0, map(nearby:'sculk_shrieker', _:0), global_state:'update_rate', 
            {
                'color' -> 0xff0000ff,
            },
            {
                'color' -> 0xff0000ff,
            }, 
            {
                'line' -> {
                    'from' -> [0.5, 0.5, 0.5],
                    'to' -> [0.5, 0.5, 0.5],
                }
            },
        );
    );
);

__draw_loop() -> (
    if(!global_state:'enabled',
        return()
    );

    __draw_overlay();
    schedule(global_state:'update_rate', '__draw_loop');
);