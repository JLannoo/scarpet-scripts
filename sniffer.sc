__config() -> {
    'commands' -> {
        'toggle' -> 'toggle',
        'rate <ticks>' -> 'set_rate',
    },
    'arguments' -> {
        'ticks' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 20,
            'suggest' -> [1, 10, 20],
        },
    },
    'libraries' -> {
        'draw' -> 'lib/draw.scl',
    }
};

import('draw', 'draw_line_from_entity_to_blocks');

global_state = {
    'enabled' -> false,
    'update_rate' -> 10,
    'sniffer_color' -> {},
};

set_rate(ticks) -> (
    global_state:'update_rate' = ticks;
    print('Update rate set to ' + ticks + ' ticks.');
);

toggle() -> (
    global_state:'enabled' = !global_state:'enabled';
    print('Sniffer tracking is now ' + if(global_state:'enabled', 'enabled', 'disabled'));
    if(global_state:'enabled', schedule(0, '__draw_loop'));
);

__draw_overlay() -> (    
    sniffers = entity_list('sniffer');
    if(length(sniffers) == 0,
        return()
    );

    for(sniffers, 
        sniffer = _;
        
        // Get the correct explored positions using NBT path
        nbt_memories = query(sniffer, 'nbt', 'Brain.memories');
        memories = parse_nbt(nbt_memories);
        
        positions = memories:'minecraft:sniffer_explored_positions':'value';
        cooldown = memories:'minecraft:sniff_cooldown':'ttl';

        // Assign color to sniffer if not already assigned
        sniffer_id = str(sniffer ~ 'id');
        if(!(global_state:'sniffer_color':sniffer_id),
            color = bitwise_or(rand(0xffffffff), 0x000000ff);
            global_state:'sniffer_color':sniffer_id = color;
        );
        
        // for(positions,
        //     position_data = _;
        //     dimension = position_data:'dimension';
        //     coords = position_data:'pos';
            
        //     // Highlight the block
        //     if(dimension == 'minecraft:overworld', 
        //         draw_shape('box', global_state:'update_rate', {
        //             'color' -> global_state:'sniffer_color':sniffer_id,
        //             'from' -> coords,
        //             'to' -> [coords:0 + 1, coords:1 + 1, coords:2 + 1],
        //             'fill' -> true,
        //         });
        //         draw_shape('line', global_state:'update_rate', {
        //             'color' -> global_state:'sniffer_color':sniffer_id,
        //             'from' -> pos(sniffer) + [0, 1.5, 0],
        //             'to' -> coords + [0.5, 1, 0.5],
        //         });
            
        //     );
        // );
        blocks = map(positions, block(_:'pos'));
        if(length(blocks) == 0, return());

        draw_line_from_entity_to_blocks(sniffer, blocks, global_state:'update_rate', {
            'color' -> global_state:'sniffer_color':sniffer_id,
            'fill' -> true,
        }, {
            'color' -> global_state:'sniffer_color':sniffer_id,
        }, {
            'line' -> {
                'from' -> [0, 2, 0],
                'to' -> [0.5, 1, 0.5],
            },
        });


        color_str = '#' + upper(str('%x', global_state:'sniffer_color':sniffer_id));

        label_lines = [];
        label_lines += 'cooldown: ' + format(if(cooldown != null, 'br ' + cooldown, 'be 0'));
        label_lines += 'explored: ' + format(color_str + ' ' + length(positions));

        for(label_lines,
            line = _;
            draw_shape('label', global_state:'update_rate', {
                'pos' -> pos(sniffer) + [0, 3, 0],
                'text' -> line,
                'fill' -> 0x00000088,
                'height' -> -_i,
                'align' -> 'left',
            });
        );
    );
);

__draw_loop() -> (
    if(!(global_state:'enabled'), return());
    
    // Draw the overlay
    __draw_overlay();
    
    // Schedule the next loop
    schedule(global_state:'update_rate', '__draw_loop');
);