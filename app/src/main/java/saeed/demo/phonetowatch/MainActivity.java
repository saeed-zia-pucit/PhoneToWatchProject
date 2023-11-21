package saeed.demo.phonetowatch;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.wearengine.HiWear;
import com.huawei.wearengine.auth.AuthCallback;
import com.huawei.wearengine.auth.AuthClient;
import com.huawei.wearengine.auth.Permission;
import com.huawei.wearengine.device.Device;
import com.huawei.wearengine.device.DeviceClient;
import com.huawei.wearengine.p2p.Message;
import com.huawei.wearengine.p2p.P2pClient;
import com.huawei.wearengine.p2p.PingCallback;
import com.huawei.wearengine.p2p.SendCallback;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Device connectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();

        searchAvailableDevices();
        checkCurrentConnectedDevice();
    }

    private void initUi() {
        findViewById(R.id.send).setOnClickListener(this);
//        findViewById(R.id.send).setOnClickListener(this);
//        findViewById(R.id.send).setOnClickListener(this);
//        findViewById(R.id.send).setOnClickListener(this);
    }

    private void searchAvailableDevices() {
        DeviceClient deviceClient = HiWear.getDeviceClient(this);
        deviceClient.hasAvailableDevices().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                checkPermissionGranted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void checkPermissionGranted() {
        AuthClient authClient = HiWear.getAuthClient(this);
        authClient.checkPermission(Permission.DEVICE_MANAGER).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    askPermission();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void askPermission() {
        AuthClient authClient = HiWear.getAuthClient(this);
        AuthCallback authCallback = new AuthCallback() {
            @Override
            public void onOk(Permission[] permissions) {
                if (permissions.length != 0) {
                    checkCurrentConnectedDevice();
                }
            }

            @Override
            public void onCancel() {
            }
        };

        authClient.requestPermission(authCallback, Permission.DEVICE_MANAGER)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void successVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                    }
                });
    }

    private void checkCurrentConnectedDevice() {
        final List<Device> deviceList = new ArrayList<>();
        DeviceClient deviceClient = HiWear.getDeviceClient(this);
        deviceClient.getBondedDevices()
                .addOnSuccessListener(new OnSuccessListener<List<Device>>() {
                    @Override
                    public void onSuccess(List<Device> devices) {
                        deviceList.addAll(devices);
                        if (!deviceList.isEmpty()) {
                            for (Device device : deviceList) {
                                if (device.isConnected()) {
                                    connectedDevice = device;
                                }
                            }
                        }
                        if (connectedDevice != null) {
                            checkAppInstalledInWatch(connectedDevice);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Process logic when the device list fails to be obtained
                        e.printStackTrace();
                    }
                });


    }

    private void checkAppInstalledInWatch(final Device connectedDevice) {
        P2pClient p2pClient = HiWear.getP2pClient(this);

        String peerPkgName = "com.wearengine.huawei";
        p2pClient.setPeerPkgName(peerPkgName);

        if (connectedDevice != null && connectedDevice.isConnected()) {
            p2pClient.ping(connectedDevice, new PingCallback() {
                @Override
                public void onPingResult(int errCode) {
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void successVoid) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                }
            });
        }
    }

    private void sendMessageToWatch(String message, Device connectedDevice) {
        P2pClient p2pClient = HiWear.getP2pClient(this);

        String peerPkgName = "com.wearengine.huawei";
        p2pClient.setPeerPkgName(peerPkgName);

        String peerFingerPrint = "com.wearengine.huawei_BALgPWTbV2CKZ9swMfG1n9ReRlQFq*************";
        p2pClient.setPeerFingerPrint(peerFingerPrint);

        Message.Builder builder = new Message.Builder();
        builder.setPayload(message.getBytes(StandardCharsets.UTF_8));
        Message sendMessage = builder.build();

        SendCallback sendCallback = new SendCallback() {
            @Override
            public void onSendResult(int resultCode) {
            }

            @Override
            public void onSendProgress(long progress) {
            }
        };
        if (connectedDevice != null && connectedDevice.isConnected() && sendMessage != null && sendCallback != null) {
            p2pClient.send(connectedDevice, sendMessage, sendCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Related processing logic for your app after the send command runs
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            //Related processing logic for your app after the send command fails to run
                        }
                    });
        }
    }

    @Override
    public void onClick(View view) {
        sendMessageToWatch("Down", connectedDevice);
    }

}

