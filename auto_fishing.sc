__config() -> {
    'commands' -> {
        'start' -> 'start_auto_fishing',
        'stop' -> 'stop_auto_fishing',
        // 'drop' -> 'drop',
    },
};

__on_start() -> (
    start_auto_fishing();
);

__on_close() -> (
    stop_auto_fishing();
);

global_state = {
    enabled -> false,
    warning_cooldown -> 0,
    has_fishing_rod -> false,
};

start_auto_fishing() -> (
    if(global_state:'enabled', (
        print('Auto fishing is already running.');
        return();
    ));
    
    print('Auto fishing started. Use /stop to stop.');

    p = player();

    run('execute as ' + p + ' at @s run player fisher spawn');

    // Check if the fisher has a fishing rod
    task(_() -> (
        sleep(500);
        fisher = player('fisher');
        global_state:'has_fishing_rod' = inventory_find(fisher, 'minecraft:fishing_rod') != null;
    ));
    global_state:'enabled' = true;

    schedule(20, '__fishing_loop');
);

stop_auto_fishing() -> (    
    global_state:'enabled' = false;
    run('player fisher kill');
    print('Auto fishing stopped.');
);

__fishing_loop() -> (
    if(!global_state:'enabled', return());
    if(!global_state:'has_fishing_rod', (
        print('Fisher is missing a fishing rod. Give them one to continue.');
        return();
    ));

    fisher = player('fisher');
    if(!fisher, (
        print('Fisher is missing. Stopping auto fishing.');
        stop_auto_fishing();
        return();
    ));

    // Fist cast
    bobber = entity_list('fishing_bobber'):0;
    if(!bobber, (
        run('player fisher use'); // Cast the line
        schedule(80, '__fishing_loop'); // Wait before next check
        return();
    ));

    // Treasure is possible (check if area around bobber (4x4x2 centered in bobber) is water, air or lilypads)
    // bobber_pos = pos(bobber);
    // scan(bobber_pos, [2, 2, 2],
    //     allowed_blocks = ['water', 'air', 'lily_pad'];
    //     if(!(allowed_blocks ~ _),
    //         if(global_state:'warning_cooldown' == 0, (
    //             print('Warning: Treasure not possible here');
    //             global_state:'warning_cooldown' = 2000; // Cooldown for next warning
    //         ));
    //     );
    // );
    // global_state:'warning_cooldown' = max(0, global_state:'warning_cooldown' - 1); // Decrease cooldown

    // Bite detection
    bobber_motion = query(bobber, 'nbt', 'Motion[1]');
    if(abs(bobber_motion) > 0.1, (
        print('Bite detected! Reeling in...');
        run('player fisher use'); // Reel in
        schedule(20, '__fishing_loop'); // Wait before next check
        return();
    ));

    // No bite detected, continue fishing
    schedule(1, '__fishing_loop');
);

__on_player_picks_up_item(p, item) -> (
    if(p != player('fisher'), return());
    if(item:0 == 'fishing_rod' && global_state:'has_fishing_rod' == false,
        global_state:'has_fishing_rod' = true;
        print('Fisher has picked up a fishing rod.');
        schedule(20, '__fishing_loop'); // Resume fishing
        return();
    );

    allowed_items = ['fishing_rod', 'enchanted_book', 'nautilus_shell', 'name_tag'];

    if((allowed_items ~ (item:0)) == null, 
        item_slot = inventory_find(p, item:0);
        run('player fisher dropStack ' + item_slot);
    ,
        run('tell @a Hey! I picked up a: ' + item:0);
        
    );   
)