package scrabble;

public abstract class Parsing {
    public static Tile[] parseTiles(String tiles) {
        if(tiles.equals("_")) {
            return new Tile[0];
        }
        String[] tileStrings = tiles.split(",");
        Tile[] parsedTiles = new Tile[tileStrings.length];

        for (int i = 0; i < tileStrings.length; i++) {
            String tileString = tileStrings[i];
            char letter = tileString.charAt(0);
            parsedTiles[i] = new Tile(letter);
        }

        return parsedTiles;
    }
}
