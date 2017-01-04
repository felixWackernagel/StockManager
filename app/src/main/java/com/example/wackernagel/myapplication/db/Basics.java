package com.example.wackernagel.myapplication.db;

import java.util.Date;

import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.NotNull;

class Basics {

    @NotNull
    @Default( value = "(datetime('now', 'localtime'))" )
    Date created;

    @NotNull
    @Default( value = "(datetime('now', 'localtime'))" )
    Date changed;

    @NotNull
    String type;

}
