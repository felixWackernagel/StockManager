package com.example.wackernagel.myapplication.db;

import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;

@Contract(authority = DataProvider.AUTHORITY)
class Category extends Basics {

    @Unique( group = 1 )
    @NotNull
    @Default( value = "0" )
    Category parent;

    @Unique( group = 1 )
    @NotNull
    String name;
}
