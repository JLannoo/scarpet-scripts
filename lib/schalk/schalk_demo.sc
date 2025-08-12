// Demo for lib/schalk.scl
// Load with: /script load schalk_demo

__config() -> {
    'commands' -> {
        'test' -> 'test',
    },
};

import('schalk', 'schalk', 'schalk_rgb', 'schalk_hex', 'schalk_component', 'schalk_join', 'schalk_hover', 'schalk_url', 'schalk_suggest', 'schalk_command', 'schalk_copy', 'schalk_hover', 'schalk_url', 'schalk_suggest', 'schalk_command', 'schalk_copy', 'schalk_with', 'schalk_apply');

test() -> (
    print(schalk('green bold underline', 'Hello from Schalk for Scarpet!'));
    print(schalk_rgb(255,00,255, 'RGB color example'));
    print(schalk_hex('#FF8800', 'Hex orange example'));

    // Compose multiple styled segments
    parts = [
        schalk_component('yellow', 'CPU: '),
        schalk_component('greenBright bold', '40% '), 
        schalk_component('lightgreen', '(ok)')
    ];
    print(schalk_join(parts));

    // Hover and URL decorator
    print(
        schalk_join([
            schalk_component('lightblue underline', 'Hover me'),
            schalk_hover('gray italic', 'I am a tooltip'),
        ])
    );
    print(
        schalk_join([
            schalk_component('white', 'Click to visit'),
            schalk_hover('gray italic', 'https://example.com'),
            schalk_url('https://example.com'),
        ])
    );

    // Suggest / Command / Copy decorators
    print(
        schalk_join([
            schalk_component('white', '[suggest /time set day]'),
            schalk_suggest('/time set day'),
        ])
    );
    print(
        schalk_join([
            schalk_component('white', '[run /kill @e[type=item]]'),
            schalk_command('/kill @e[type=item]'),
        ])
    );
    print(
        schalk_join([
            schalk_component('white', '[copy coords]'),
            schalk_copy(str('X:%d Y:%d Z:%d', 0, 80, 0))
        ])
    );

    print(schalk_join([
        schalk_component('white', 'Click '),

        schalk_component('lightblue underline bold', '[HERE]'),
        schalk_hover('yellow italic', 'Awesome!'),
        schalk_command('/kill'),

        schalk_component('white', ' - Button to win $1000'),
        schalk_hover('gray italic', 'This is actually a lie'),
    ]));
);

__on_start() -> (
    test();
);
