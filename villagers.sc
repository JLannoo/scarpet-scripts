__config() -> {
    'commands' -> {
        'job_site' -> 'job_site_toggle',
        'rate <ticks>' -> 'set_rate',
    },
    'libraries' -> {
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

import('draw', 'draw_line_from_entity_to_blocks');

global_state = {
    'villagers' -> [],
    'enabled' -> {
        'job_site' -> false,
    },
    'update_rate' -> 10,
};

job_site_toggle() -> (
    global_state:'enabled':'job_site' = !global_state:'enabled':'job_site';
    print('job_site tracking is now ' + if(global_state:'enabled':'job_site', 'enabled', 'disabled'));
    
    __draw_loop();
);

set_rate(ticks) -> (
    global_state:'update_rate' = ticks;
    print('Update rate set to ' + ticks + ' ticks.');
);

__draw_loop() -> (
    if(global_state:'enabled':'job_site', __draw_job_site_overlay());

    any_enabled = false;
    for(global_state:'enabled', 
        enabled = _; 
        if(enabled, any_enabled = true);
    );
    if(!any_enabled, return());

    schedule(global_state:'update_rate', '__draw_loop');
);

__draw_job_site_overlay() -> (
    global_state:'villagers' = entity_list('villager');
    villagers = global_state:'villagers';
    if(length(villagers) == 0,
        return()
    );

    for(villagers, 
        villager = _;
        
        // Get the villager's job_site block
        job_site = query(villager, 'brain', 'job_site');
        if(job_site == null, continue());
        
        // Draw a line from the villager to their job_site
        draw_line_from_entity_to_blocks(villager, block(job_site:1), global_state:'update_rate', null, null, {
            'line' -> {
                'from' -> [0, (villager~'height')/2, 0],
                'to' -> [0.5, 0.5, 0.5],
            }
        });
    );
);