package insectsrobotics.visualnavigationapp.NavigationModules._Superclasses;

/**
 * This Interface makes sure all navigation modules have the same structure to ensure compatibility.
 * It is implemented in the Superclass of the navigation Module.
 */
public interface VisualNavigationInterface {

    void setupLearningAlgorithm(int[] image);

    void learnImage(int[] image);

    double calculateFamiliarity(int[] image);

}
