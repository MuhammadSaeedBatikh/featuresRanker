import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;

/**
 * Created by Muhammad on 17/07/2017.
 */
public class Sign {
    // static enum Group {BACKHAND, FOREHAND}

    boolean staticGesture;
    boolean phalangesAreImportant;
    float letterFrequency = 0;
    MyVector n;
    MyVector d;
    MyVector[] fingersDirections = new MyVector[5];
    MyVector[] distalPhalangesDirections = new MyVector[5];
    MyVector[] proximalPhalangesDirections = new MyVector[5];
    float[] handTofingertipsDistances = new float[5]; // distance from the center of the hand to each fingertip normalized to each finger length

    //cos(angle) = dot(a,b)/(||a|| ||b||), a and b are the proximal Vectors
    float[] anglesBetweenAdjacentFingers = new float[4]; // a[0]=angle between pinky and ring, a[1] = angle between ring and middle etc
    /* float pitch;   //angle around the x-axis
    float yaw;     //angle around the y-axis
     float roll;    //angle around the z-axis
     // The Vector class defines functions for getting the pitch, yaw, and roll
 */
    float grabAngle;
     /*The angle is computed by looking at the angle between the direction of the 4 fingers and the direction of the hand.
     Thumb is not considered when computing the angle.
     The angle is 0 radian for an open hand, and reaches pi radians when the pose is a tight fist.*/

    float pinchDistance; // the shortest distance between the last 2 phalanges of the thumb and those of the index finger
    float[] signFeatures = new float[52];


    public Sign(Sign sign) {
        this.staticGesture = sign.staticGesture;
        this.phalangesAreImportant = sign.phalangesAreImportant;
        this.letterFrequency = sign.letterFrequency;
        this.fingersDirections = sign.fingersDirections;
        this.distalPhalangesDirections = sign.distalPhalangesDirections;
        this.proximalPhalangesDirections = sign.proximalPhalangesDirections;
        this.handTofingertipsDistances = sign.handTofingertipsDistances;
        this.anglesBetweenAdjacentFingers = sign.anglesBetweenAdjacentFingers;
        this.grabAngle = sign.grabAngle;
        this.pinchDistance = sign.pinchDistance;
    }

    public Sign(Hand hand) {


        //   System.out.println(Arrays.toString(fingersDirections) + "\n" + Arrays.toString(proximalPhalangesDirections));
      /*  for (int i = 0; i < 4; i++) {
            Vector current = proximalPhalangesDirections[i];
            Vector next = proximalPhalangesDirections[i + 1];
            float angle = current.angleTo(next);
            this.anglesBetweenAdjacentFingers[i] = angle;
        }*/
        gatherDataFromHand(hand);

        fillFeaturesArray();

    }

    public void gatherDataFromHand(Hand hand) {
        try {
            System.out.println("in gatherData");
            fingersDirections = new MyVector[5];
            distalPhalangesDirections = new MyVector[5];
            proximalPhalangesDirections = new MyVector[5];
            handTofingertipsDistances = new float[5];
            anglesBetweenAdjacentFingers = new float[4];
            MyVector d = new MyVector(hand.direction());
            MyVector n = new MyVector(hand.palmNormal());
            MyVector r = d.cross(n).normalized();
            int count = hand.fingers().count();
            for (int i = 0; i < count; i++) {
                Finger finger = hand.finger(i);
                System.out.println(finger.direction());
                MyVector v = changeBasis(d, r, n, new MyVector(finger.direction()));
                System.out.println("v.getX " + v.getX());
                this.fingersDirections[i] = v;
                this.distalPhalangesDirections[i] = changeBasis(d, r, n, new MyVector(finger.bone(Bone.Type.TYPE_DISTAL).direction()));
                this.proximalPhalangesDirections[i] = changeBasis(d, r, n, new MyVector(finger.bone(Bone.Type.TYPE_PROXIMAL).direction()));
                this.handTofingertipsDistances[i] = finger.tipPosition().
                        minus(hand.palmPosition()).magnitude() / finger.length();
            }
            this.grabAngle = hand.grabAngle();
            this.pinchDistance = hand.pinchDistance();
        } catch (NullPointerException e) {
            System.err.println("Exception");
            gatherDataFromHand(hand);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static MyVector changeBasis(MyVector d, MyVector r, MyVector n, MyVector v) {
        float[] c = {d.getX(), d.getY(), d.getZ(), r.getX(), r.getY(), r.getZ(), n.getX(), n.getY(), n.getZ()};
        // h = 1/det
        float h = 1 / (c[0] * (c[4] * c[8] - c[5] * c[7]) + c[1] * (c[5] * c[6] - c[3] * c[8]) + c[2] * (c[3] * c[7] - c[4] * c[6]));
        float[] inverseOfBasis = {

                h * (c[4] * c[8] - c[5] * c[7]), h * (c[2] * c[7] - c[1] * c[8]), h * (c[1] * c[5] - c[2] * c[4]),

                h * (c[5] * c[6] - c[3] * c[8]), h * (c[0] * c[8] - c[2] * c[6]), h * (c[2] * c[3] - c[0] * c[5]),

                h * (c[3] * c[7] - c[4] * c[6]), h * (c[1] * c[6] - c[0] * c[7]), h * (c[0] * c[4] - c[1] * c[3])
        };
        MyVector column1 = new MyVector(inverseOfBasis[0], inverseOfBasis[1], inverseOfBasis[2]);
        MyVector column2 = new MyVector(inverseOfBasis[3], inverseOfBasis[4], inverseOfBasis[5]);
        MyVector column3 = new MyVector(inverseOfBasis[6], inverseOfBasis[7], inverseOfBasis[8]);
        //column1*x + column2*y +column3*z
        return column1.times(v.getX()).plus(column2.times(v.getY())).plus(column3.times(v.getZ()));
    }

    public static Vector changeBasis(Vector d, Vector r, Vector n, Vector v) {
        float[] c = {d.getX(), d.getY(), d.getZ(), r.getX(), r.getY(), r.getZ(), n.getX(), n.getY(), n.getZ()};
        // h = 1/det
        float h = 1 / (c[0] * (c[4] * c[8] - c[5] * c[7]) + c[1] * (c[5] * c[6] - c[3] * c[8]) + c[2] * (c[3] * c[7] - c[4] * c[6]));
        float[] inverseOfBasis = {

                h * (c[4] * c[8] - c[5] * c[7]), h * (c[2] * c[7] - c[1] * c[8]), h * (c[1] * c[5] - c[2] * c[4]),

                h * (c[5] * c[6] - c[3] * c[8]), h * (c[0] * c[8] - c[2] * c[6]), h * (c[2] * c[3] - c[0] * c[5]),

                h * (c[3] * c[7] - c[4] * c[6]), h * (c[1] * c[6] - c[0] * c[7]), h * (c[0] * c[4] - c[1] * c[3])
        };
        Vector column1 = new Vector(inverseOfBasis[0], inverseOfBasis[1], inverseOfBasis[2]);
        Vector column2 = new Vector(inverseOfBasis[3], inverseOfBasis[4], inverseOfBasis[5]);
        Vector column3 = new Vector(inverseOfBasis[6], inverseOfBasis[7], inverseOfBasis[8]);
        //column1*x + column2*y +column3*z
        return column1.times(v.getX()).plus(column2.times(v.getY())).plus(column3.times(v.getZ()));
    }

    public String toCSV() {
        System.out.println("in toCSV() ");
        String result = "";
        for (int i = 0; i < this.signFeatures.length - 1; i++) {
            result += signFeatures[i] + ",";
        }
        result += this.signFeatures[this.signFeatures.length - 1];
        return result;
    }

    public static String vectorAnglestoCSV(Vector vector) {
        String st = (int) Math.toDegrees(vector.pitch()) + "," + (int) Math.toDegrees(vector.yaw()) + "," + (int) Math.toDegrees(vector.roll());
        return st;
    }

    public static String vectorCoordinatesToCSV(Vector vector) {
        String st = vector.getX() + "," + vector.getY() + "," + vector.getZ();
        return st;
    }

    public static int[] vectorAnglesToArray(Vector vector) {
        int[] angles = {(int) Math.toDegrees(vector.pitch()), (int) Math.toDegrees(vector.yaw()), (int) Math.toDegrees(vector.roll())};
        return angles;
    }


    public static int[] subtractAngles(Vector v1, Vector v2) {
        int[] angles1 = vectorAnglesToArray(v1);
        int[] angles2 = vectorAnglesToArray(v2);
        int[] anglesDif = {angles1[0] - angles2[0], angles1[1] - angles2[1], angles1[2] - angles2[2]};
        return anglesDif;
    }

    public void fillFeaturesArray() {
        System.out.println("filling");
        int j = 0;
        for (int i = 0; i < 5; i++) {
            this.signFeatures[j] = this.fingersDirections[i].getX();
            this.signFeatures[j + 1] = this.fingersDirections[i].getY();
            this.signFeatures[j + 2] = this.fingersDirections[i].getZ();
            j += 3;
        }
        for (int i = 0; i < 5; i++) {
            this.signFeatures[j] = this.distalPhalangesDirections[i].getX();
            this.signFeatures[j + 1] = this.distalPhalangesDirections[i].getY();
            this.signFeatures[j + 2] = this.distalPhalangesDirections[i].getZ();
            j += 3;
        }
        for (int i = 0; i < 5; i++) {
            this.signFeatures[j] = this.proximalPhalangesDirections[i].getX();
            this.signFeatures[j + 1] = this.proximalPhalangesDirections[i].getY();
            this.signFeatures[j + 2] = this.proximalPhalangesDirections[i].getZ();
            j += 3;
        }
        for (int i = 0; i < 5; i++) {
            this.signFeatures[i + 45] = this.handTofingertipsDistances[i];
        }
        this.signFeatures[50] = this.grabAngle;
        this.signFeatures[51] = this.pinchDistance;
        System.out.println("filled");
    }
    /*
    *  boolean staticGesture;
    boolean phalangesAreImportant;
    float letterFrequency = 0;
    Vector[] fingersDirections = new Vector[5];
    Vector[] distalPhalangesDirections = new Vector[5];
    Vector[] proximalPhalangesDirections = new Vector[5];
    Vector armDirection = new Vector(); //The normalized direction in which the arm is pointing (from elbow to wrist)
    float[] handTofingertipsDistances = new float[5]; // distance from the center of the hand to each fingertip normalized to each finger length
   float[] anglesBetweenAdjacentFingers = new float[4]; // a[0]=angle between pinky and ring, a[1] = angle between ring and middle etc
    float grabAngle;

    float pinchDistance; // the shortest distance between the last 2 phalanges of the thumb and those of the index finger
  */
    /*public float compareSign(Sign sign){

    }*/
}