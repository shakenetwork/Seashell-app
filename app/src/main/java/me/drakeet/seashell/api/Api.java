package me.drakeet.seashell.api;

/**
 * Created by drakeet on 10/22/14.
 */
public class Api {
    private static final String HOST = "http://test.drakeet.me/?key=";

    private static final String HOST_2 = "http://121.40.208.24/index.php";

    private static final String CHECK_UPDATE = HOST + "s_check_update";

    public static final String GET_WORD = HOST_2 + "/Word/getWord?id=%1$s";

    public static final String GET_NOTE = HOST + "s_note";
}
