using UnityEngine;
using UnityEngine.Android;
using UnityEngine.SpatialTracking;
using System.Collections.Generic;

public class BluetoothPluginController : MonoBehaviour
{
    private const float X_MAX = 3.65f;
    private const float X_MIN = 0.10f;
    private const float Y_MAX = 2.67f;
    private const float Y_MIN = 0.13f;
    private const float Z_MAX = 3.71f;
    private const float Z_MIN = -1.12f;
    private const float TIME_DELTA = 0.1f;

    public GameObject cameraRig;
    private AndroidJavaObject bluetoothService;
    private AndroidJavaObject activityContext;
    private Vector3 newPosition = new Vector3(1.87f, 1.7f, 1.0f);
    private Vector3 velocity = Vector3.zero;
    

    void Start()
    {
        // Request Android Permissions for Bluetooth Connectivity
        Permission.RequestUserPermission("android.permission.BLUETOOTH");
        Permission.RequestUserPermission("android.permission.BLUETOOTH_ADMIN");
        Permission.RequestUserPermission("android.permission.ACCESS_FINE_LOCATION");
        Permission.RequestUserPermission("android.permission.ACCESS_COARSE_LOCATION");

        // Get UnityActivity context to intialize BluetoothService
        using(AndroidJavaClass activityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
            activityContext = activityClass.GetStatic<AndroidJavaObject>("currentActivity");
        }

        // Initialize BluetoothService with activityContext and start plugin
        using(bluetoothService = new AndroidJavaObject("de.MaxBauer.UWBRTLSVR.BluetoothService")) {
            bluetoothService.Call("setContext", activityContext);
            bluetoothService.Call("connectToTag");
        }
    }

    void Update()
    {
        // Translate the camera from the current position to the new estimated position
        cameraRig.transform.position = Vector3.SmoothDamp(cameraRig.transform.position, newPosition, ref velocity, TIME_DELTA);
    }

    // Callback from Android Plugin with filtered position updates
    void onMessageReceived(string message)
    {
        string[] data = message.Split(',');
        float x = float.Parse(data[0]);
        float z = float.Parse(data[1]);
        float y = float.Parse(data[2]);
        
        boundCheck(ref x, ref y, ref z);
        newPosition = new Vector3(x, y, z);
    }

    // Make sure that invalid position data is not applied
    private void boundCheck(ref float x, ref float y, ref float z)
    {
        if (x > X_MAX){
            x = X_MAX;
        } else if (x < X_MIN){
            x = X_MIN;
        }

        if (y > Y_MAX){
            y = Y_MAX;
        } else if (y < Y_MIN){
            y = Y_MIN;
        }

        if (z > Z_MAX){
            z = Z_MAX;
        } else if (z < Z_MIN){
            z = Z_MIN;
        }
    }
}