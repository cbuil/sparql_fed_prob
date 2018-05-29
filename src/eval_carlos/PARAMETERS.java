package eval_carlos;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;

@SuppressWarnings("deprecation")
public class PARAMETERS
{

    /*
     * Commandline parameters
     */
    protected static final String PARAM_HELP = "?";
    protected static final String PARAM_HELP1 = "h";

    /**
     * HELP
     */

    static final OptionGroup OPTIONGROUP_HELP = new OptionGroup();

    static
    {
        OptionBuilder.withArgName("help");
        OptionBuilder.hasArgs(0);
        OptionBuilder
                .withDescription("print help screen");
        OPTIONGROUP_HELP.addOption(OptionBuilder.create(PARAM_HELP));
        OptionBuilder.withArgName("help");
        OptionBuilder.hasArgs(0);
        OptionBuilder
                .withDescription("print help screen");
        OPTIONGROUP_HELP.addOption(OptionBuilder.create(PARAM_HELP1));
    }

    protected static final String PARAM_DATA_DIR = "d";
    public static final Option OPTION_DATA_DIR = OptionBuilder.create(PARAM_DATA_DIR);

    protected static final String PARAM_INPUT = "i";
    public static final Option OPTION_INPUT = OptionBuilder.create(PARAM_INPUT);

    protected static final String PARAM_CARD = "c";
    public static final Option OPTION_CARD = OptionBuilder.create(PARAM_CARD);

    protected static final String PARAM_JOIN = "j";
    public static final Option OPTION_JOIN = OptionBuilder.create(PARAM_JOIN);

    protected static final String PARAM_OUTPUT_DIR = "o";
    public static final Option OPTION_OUTPUT_DIR = OptionBuilder
            .create(PARAM_OUTPUT_DIR);

    public static final String PARAM_ENDPOINTS = "e";
    public static final Option OPTION_ENDPOINTS = OptionBuilder.create(PARAM_ENDPOINTS);

    public static final String PARAM_SUBQUERIES = "q";
    public static final Option OPTION_SUBQUERIES = OptionBuilder.create(PARAM_SUBQUERIES);
    public static final String PARAM_DEBUG = "l";
    public static final Option OPTION_DEBUG = OptionBuilder.create(PARAM_DEBUG);
    public static final String PARAM_BATCHSIZE = "b";
    public static final Option OPTION_BATCHSIZE = OptionBuilder.create(PARAM_BATCHSIZE);

}
