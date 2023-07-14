package com.jsancre.gameverse.providers;

import android.content.Context;

import com.jsancre.gameverse.R;

import java.util.ArrayList;
import java.util.List;

public class TitleProvider {
    private static List<String> mtitleList = new ArrayList<>();

    public static void addTitle(String title) {
        mtitleList.add(title);
    }

    public static void removeTitle(String title) {
        mtitleList.remove(title);
    }
    public static List<String> getTitleList(Context context) {
        List<String> titleList = new ArrayList<>();
        titleList.add(context.getString(R.string.select_title_prompt));
        titleList.addAll(mtitleList);

        titleList.add("A Way Out");
        titleList.add("Among Us");
        titleList.add("Animal Crossing: New Horizons");
        titleList.add("Apex Legends");
        titleList.add("Assassins Creed Brotherhood");
        titleList.add("Assassins Creed Odyssey");
        titleList.add("Assassins Creed Valhalla");
        titleList.add("Bayonetta 2");
        titleList.add("Battlefield 1");
        titleList.add("Battlefield 2");
        titleList.add("Battlefield 3");
        titleList.add("Battlefield 4");
        titleList.add("Battlefield 2042");
        titleList.add("BioShock");
        titleList.add("Bioshock Infinite");
        titleList.add("Bloodborne");
        titleList.add("Borderlands 3");
        titleList.add("Breath of the Wild");
        titleList.add("Call of Duty: Black Ops Cold War");
        titleList.add("Call of Duty: Warzone");
        titleList.add("Celeste");
        titleList.add("Counter-Strike: Global Offensive");
        titleList.add("Cuphead");
        titleList.add("Cyberpunk 2077");
        titleList.add("Darksiders Genesis");
        titleList.add("Dark Souls II");
        titleList.add("Dark Souls III");
        titleList.add("Days Gone");
        titleList.add("Dead by Daylight");
        titleList.add("Death Stranding");
        titleList.add("Deathloop");
        titleList.add("Destiny 2");
        titleList.add("Devil May Cry 5");
        titleList.add("Detroit: Become Human");
        titleList.add("Disco Elysium");
        titleList.add("Dishonored 2");
        titleList.add("Diablo I");
        titleList.add("Diablo II");
        titleList.add("Diablo III");
        titleList.add("Diablo IV");
        titleList.add("Doom (2016)");
        titleList.add("Doom Eternal");
        titleList.add("Dragon Age: Inquisition");
        titleList.add("Dragon Ball FighterZ");
        titleList.add("Dragon Ball Z: Kakarot");
        titleList.add("Dragon Quest XI");
        titleList.add("Enter the Gungeon");
        titleList.add("Fall Guys: Ultimate Knockout");
        titleList.add("Fallout 4");
        titleList.add("Far Cry 6");
        titleList.add("FIFA 22");
        titleList.add("Final Fantasy VII Remake");
        titleList.add("Final Fantasy XIV");
        titleList.add("Forza Horizon 4");
        titleList.add("Gears of War 5");
        titleList.add("Ghost of Tsushima");
        titleList.add("God of War");
        titleList.add("Grand Theft Auto V");
        titleList.add("Hades");
        titleList.add("Halo Infinite");
        titleList.add("Horizon Zero Dawn");
        titleList.add("Injustice 2");
        titleList.add("League of Legends");
        titleList.add("Mario Kart 8 Deluxe");
        titleList.add("Minecraft");
        titleList.add("Mortal Kombat 11");
        titleList.add("Mortal Kombat X");
        titleList.add("Nier: Automata");
        titleList.add("Overwatch");
        titleList.add("Persona 5");
        titleList.add("PlayerUnknowns Battlegrounds");
        titleList.add("Pokémon Sword and Shield");
        titleList.add("Ratchet and Clank: Rift Apart");
        titleList.add("Red Dead Redemption 2");
        titleList.add("Resident Evil 2 Remake");
        titleList.add("Resident Evil 3 Remake");
        titleList.add("Resident Evil Village");
        titleList.add("Sekiro: Shadows Die Twice");
        titleList.add("Skyrim");
        titleList.add("Splatoon 2");
        titleList.add("Star Wars Jedi: Fallen Order");
        titleList.add("Super Mario Odyssey");
        titleList.add("Tekken 7");
        titleList.add("The Elder Scrolls Online");
        titleList.add("The Last of Us Part II");
        titleList.add("The Legend of Zelda: Breath of the Wild");
        titleList.add("The Legend of Zelda: Links Awakening");
        titleList.add("The Outer Worlds");
        titleList.add("The Witcher 3: Wild Hunt");
        titleList.add("Titanfall 2");
        titleList.add("Uncharted 4: A Thief's End");
        titleList.add("Valorant");
        titleList.add("Watch Dogs: Legion");
        titleList.add("World of Warcraft");
        titleList.add("Yakuza: Like a Dragon");
        // Agrega el resto de los títulos aquí

        return titleList;
    }

}