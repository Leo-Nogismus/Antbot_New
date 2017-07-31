package insectrobotics.broadcastlibrary;


public interface BroadcastValues {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////Applications/////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    String ANTEYE = "imagemaipulations";
    String SERIAL = "serialcommunicationapp";
    String COMBINER = "antbotcombiner";
    String INTEGRATOR = "pathintegrator";
    String VISUAL = "visualnavigationapp";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////Service////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    String SERIAL_SERVICE = "insectsrobotics.serialcommunicationapp.SerialConnectionBackgroundServiceIntent";
    String COMBINER_SERVICE = "insectsrobotics.antbotcombiner.CombinerBackgroundServiceIntent";
    String INTEGRATOR_SERVICE = "insectsrobotics.pathintegrator.PathIntegratorBackgroundServiceIntent";
    String VISUAL_SERVICE = "insectsrobotics.visualnavigationapp.VisualNavigationBackgroundServiceIntent";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////Broadcasts//////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //______________________________________AntEye Broadcasts_______________________________________
    String SERVER_CONNECTION_BROADCAST = "insectsrobotics.imagemaipulations.serialcommunicationapp.SERVER_CONNECTION_BROADCAST";
    String IMAGE_SERVER_BROADCAST = "insectsrobotics.imagemaipulations.serialcommunicationapp.IMAGE_SERVER_BROADCAST";
    String IMAGE_BROADCAST = "insectsrobotics.imagemaipulations.visualnavigationapp.IMAGE_BROADCAST";
    String VISUAL_MODULE_BROADCAST = "insectsrobotics.imagemaipulations.visualnavigationapp.VN_MODULE_BROADCAST";
    String INTEGRATOR_MODULE_BROADCAST = "insectsrobotics.imagemaipulations.pathintegrator.PI_MODULE_BROADCAST";
    String COMBINER_MODULE_BROADCAST = "insectsrobotics.imagemaipulations.antbotcombiner.COMBINER_MODULE_BROADCAST";

    //_______________________________SerialCommunication Broadcasts_________________________________
    String WHEEL_ENCODER_BROADCAST = "insectsrobotics.serialcommunicationapp.pathintegrator.WHEEL_ENCODER_BROADCAST";
    String TASK_EXECUTED_BROADCAST = "insectsrobotics.serialcommunicationapp.antbotcombiner.TASK_EXECUTED_BROADCAST";
    String HOMING_ROUTING_BROADCAST = "insectsrobotics.serialcommunicationapp.antbotcombiner.HOMING_ROUTING_BROADCAST";
    String SERIAL_CONNECTION_ESTABLISHED_BROADCAST = "insectsrobotics.serialcommunicationapp.imagemaipulations.SERIAL_CONNECTION_ESTABLISHED_BROADCAST";
    String SERVER_CONNECTION_ESTABLISHED_BROADCAST = "insectsrobotics.serialcommunicationapp.imagemaipulations.SERVER_CONNECTION_ESTABLISHED_BROADCAST";
    String LEARN_IMAGE_COMMAND_BROADCAST = "insectsrobotics.serialcommunicationapp.visualnavigationapp.LEARN_IMAGE_COMMAND_BROADCAST";
    String RESET_LEARNING_COMMAND_BROADCAST = "insectsrobotics.serialcommunicationapp.visualnavigationapp.RESET_LEARNING_COMMAND_BROADCAST";

    //_____________________________________Combiner Broadcasts______________________________________
    String DECISION_BROADCAST = "insectsrobotics.antbotcombiner.serialcommunicationapp.DECISION_BROADCAST";
    String REQUEST_PI_DATA_BROADCAST = "insectsrobotics.antbotcombiner.pathintegrator.REQUEST_PI_DATA_BROADCAST";
    String REQUEST_VN_DATA_BROADCAST = "insectsrobotics.antbotcombiner.visualnavigationapp.REQUEST_VN_DATA_BROADCAST";

    //____________________________________Integrator Broadcasts_____________________________________
    String VECTOR_BROADCAST = "insectsrobotics.pathintegrator.antbotcombiner.VECTOR_BROADCAST";

    //_________________________________VisualNavigation Broadcasts__________________________________
    String NUMBER_OF_IMAGES_BROADCAST = "insectsrobotics.visualnavigationapp.imagemaipulations.NUMBER_OF_IMAGES_BROADCAST";
    String STATUS_UPDATE_BROADCAST = "insectsrobotics.visualnavigationapp.imagemaipulations.STATUS_UPDATE_BROADCAST";
    String ERROR_BROADCAST = "insectsrobotics.visualnavigationapp.antbotcombiner.ERROR_BROADCAST";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////Actions///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //______________________________________AntEye Broadcasts_______________________________________
    String SERVER_CONNECTION = "SERVER_CONNECTION_BROADCAST";
    String IMAGE_SERVER = "IMAGE_SERVER_BROADCAST";
    String IMAGE = "IMAGE_BROADCAST";
    String VISUAL_MODULE = "VN_MODULE_BROADCAST";
    String INTEGRATOR_MODULE = "PI_MODULE_BROADCAST";
    String COMBINER_MODULE = "COMBINER_MODULE_BROADCAST";

    //_______________________________SerialCommunication Broadcasts_________________________________
    String WHEEL_ENCODER = "WHEEL_ENCODER_BROADCAST";
    String TASK_EXECUTED = "TASK_EXECUTED_BROADCAST";
    String HOMING_ROUTING = "HOMING_ROUTING_BROADCAST";
    String SERIAL_CONNECTION_ESTABLISHED = "SERIAL_CONNECTION_ESTABLISHED_BROADCAST";
    String SERVER_CONNECTION_ESTABLISHED = "SERVER_CONNECTION_ESTABLISHED_BROADCAST";
    String LEARN_IMAGE_COMMAND = "LEARN_IMAGE_COMMAND_BROADCAST";
    String RESET_LEARNING_COMMAND = "RESET_LEARNING_COMMAND_BROADCAST";

    //_____________________________________Combiner Broadcasts______________________________________
    String DECISION = "DECISION_BROADCAST";
    String REQUEST_PI_DATA = "REQUEST_PI_DATA_BROADCAST";
    String REQUEST_VN_DATA = "REQUEST_VN_DATA_BROADCAST";

    //____________________________________Integrator Broadcasts_____________________________________
    String VECTOR = "VECTOR_BROADCAST";

    //_________________________________VisualNavigation Broadcasts__________________________________
    String NUMBER_OF_IMAGES = "NUMBER_OF_IMAGES_BROADCAST";
    String STATUS_UPDATE = "STATUS_UPDATE_BROADCAST";
    String ERROR = "ERROR_BROADCAST";


}
