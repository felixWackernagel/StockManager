package com.example.wackernagel.myapplication.db;

import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;

@Contract(authority = DataProvider.AUTHORITY)
class StockItem extends Basics {

    @Unique(group = 0)
    @NotNull
    @Default( value = "0" )
    Category category;

    @Unique(group = 0)
    @NotNull
    String name;

    @NotNull
    @Default(value = "0")
    int stock;

    @NotNull
    @Default(value = "0")
    int orderLimit;

}
