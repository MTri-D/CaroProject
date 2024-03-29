package com.example.caroproject;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caroproject.Adapter.AdapterGridview;
import com.example.caroproject.Adapter.CaroCenter;
import com.example.caroproject.Adapter.FirebaseHelper;
import com.example.caroproject.Adapter.NetworkUtils;
import com.example.caroproject.Data.MatchHistory;
import com.example.caroproject.Data.Room;
import com.example.caroproject.Data.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InGameOnlineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InGameOnlineFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public InGameOnlineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment inGameOnlineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InGameOnlineFragment newInstance(String param1, String param2) {
        InGameOnlineFragment fragment = new InGameOnlineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private int positionUpdate=-1, countRepeat = 0, countRepeat2 = 0, positionOld=0, rematchPlayer1 = 0, rematchPlayer2 = 0;
    private long noWins=0, noLosses=0;
    private ImageView imgBottom;
    private ImageButton imgTop;
    private DatabaseReference gameRef;
    private ProgressBar progressBar;
    private TextView txtWatch2, txtWatch1, txtMessage, txtUsername, txtNickname, txtNoWins, txtNoLosses;
    private boolean checkWatch = true, checkDoneRematch=false, checkClick = false, checkAgree=false, checkConnect = true, checkWin = true, checkRematch = true, checkResult=true, checkRandom=false;
    private String idRoom="", winner = "", currentPlayerOld="", message = "", messageOld="", username = "", keyMessage = "";
    private AlertDialog roomIdDialog, confirmSurrender,noInternetDialog, winnerDialog, rematchDialog, waitRematchDialog, resultRematchDialog;
    private int sizeBoard=0, countTurn=0;
    private int times=0;
    private float offsetX = 0, offsetY = 0;
    private Bundle bundle;
    private CaroCenter caroCenter = new CaroCenter();
    private GridView gridView;
    private LinearLayout linearLayout;
    private int countPlayer = 0;
    private Dialog dialog, dialogInfo;
    private String currentPlayer = "", player1 = "", player2 = "", userId = "", titleMessage = "", userIdOpponent = "";
    private boolean gameOver=false;
    private EditText editMessage;
    private Handler handler;
    private Button btnSendMessage, btnCancel, btnClose;
    private Button btnTop, btnSurrender, btnMessage, btnZoom, btnHome;
    private int[] savePlayerPosition;
    private CountDownTimer countDownTimer;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 3.0f;
    private AdapterGridview adapter;
    private View view;

    private int[][] board;  // Bảng lưu trạng thái của ô cờ

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Bundle args = getArguments();
        if (args != null) {
            // Lấy dữ liệu từ Bundle
            sizeBoard = args.getInt("sizeBoard");
            times = args.getInt("time");
            idRoom = args.getString("idRoom");
        }
        // Tạo một Bundle để chứa dữ liệu
        bundle = new Bundle();
        // Đặt dữ liệu vào Bundle, ví dụ:
        bundle.putInt("sizeBoard", sizeBoard);
        bundle.putInt("time", times);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_in_game_online, container, false);
        gameRef = FirebaseDatabase.getInstance().getReference("room/" + idRoom);

        scaleGestureDetector = new ScaleGestureDetector(requireContext(), new ScaleListener());

        gridView = view.findViewById(R.id.gridView);
        linearLayout = view.findViewById(R.id.linearGrid);
        txtWatch1 = view.findViewById(R.id.txtTopInGame);
        txtWatch2 = view.findViewById(R.id.txtBottomInGame);
        txtMessage = view.findViewById(R.id.txtMessage);
        txtUsername = view.findViewById(R.id.txtUsername);
        imgBottom = view.findViewById(R.id.imgBottomInGame);
        imgTop = view.findViewById(R.id.imgTopInGame);
        btnSurrender = view.findViewById(R.id.btnSurrenderInGame);
        progressBar=view.findViewById(R.id.progress_bar_ingame);

        adapter = new AdapterGridview(view.getContext(), sizeBoard);

        // Khởi tạo bảng cờ và bắt đầu trò chơi
        gridView.setAdapter(adapter);
        gridView.setNumColumns(sizeBoard);
        readData();

        initializeBoard(view);

        int[] locationOriginal = new int[2];
        gridView.getLocationOnScreen(locationOriginal);

        xOriginal = locationOriginal[0];
        yOriginal = locationOriginal[1];
        handler = new Handler(Looper.getMainLooper());
        int delayTime = 0;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtils.isNetworkAvailable(requireContext()) && checkConnect) {
                    checkConnect = false;
                    turnOffDialog();
                    showNoInternetDialog(view);
                }
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    checkConnect = true;
                    if (noInternetDialog != null && noInternetDialog.isShowing()) {
                        noInternetDialog.dismiss();
                    }
                }

                //vao day dung 1 lan
                if (countRepeat2 == 0 && !player2.equals("")) {
                    if (roomIdDialog != null && roomIdDialog.isShowing()) {
                        roomIdDialog.dismiss();
                    }
                    if (userId.equals(player1)) {
                        userIdOpponent = player2;
                    } else {
                        userIdOpponent = player1;
                    }
                    readDoc();
                    currentPlayerOld = player1;
                    messageOld = message;
                    if (userId.equals(player1)) {
                        checkClick = true;
                    } else {
                        checkClick = false;
                    }
                    if (!userId.equals(currentPlayer)) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        startCountdownTimer(times, txtWatch1, view);
                        imgBottom.setBackgroundResource(R.drawable.custom_picture);
                        imgTop.setBackgroundResource(R.drawable.custom_picture2);
                    } else {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        startCountdownTimer(times, txtWatch2, view);
                        imgTop.setBackgroundResource(R.drawable.custom_picture);
                        imgBottom.setBackgroundResource(R.drawable.custom_picture2);
                    }
                    countRepeat2++;
                }

                //send message
                if (!message.isEmpty() && message != messageOld) {
                    txtMessage.setVisibility(View.VISIBLE);
                    if (userId.equals(keyMessage)) {
                        txtMessage.setText("You: " + message);
                    } else {
                        txtMessage.setText(username + ": " + message);
                    }
                } else {
                    txtMessage.setVisibility(View.INVISIBLE);
                }

                //set nickname
                if (username == null) {
                    username = "No name";
                }
                txtUsername.setText(username);

                //rematch
                if (userId.equals(player1) && checkRematch) {
                    if (rematchPlayer2 == 1) {
                        checkRematch = false;
                        turnOffDialog();
                        showRematchDialog(view);
                    }
                }
                if (userId.equals(player2) && checkRematch) {
                    if (rematchPlayer1 == 1) {
                        checkRematch = false;
                        turnOffDialog();
                        showRematchDialog(view);
                    }
                }

                //start rematch
                if (rematchPlayer1 == 1 && rematchPlayer2 == 1) {
                    progressBar.setVisibility(View.VISIBLE);
                    checkAgree = true;
                    turnOffDialog();

                    if (!winner.isEmpty()) {
                        Room room = new Room(checkRandom, true, 0, 0, "", "", times, player2, sizeBoard, -1, player2, player1, idRoom, "", false, 0);
                        gameRef.setValue(room, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                initializeBoard(view);
                                readDoc();
                                if (userId.equals(currentPlayer)) {
                                    checkClick = true;
                                } else {
                                    checkClick = false;
                                }
                                if (!userId.equals(currentPlayer)) {
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }
                                    startCountdownTimer(times, txtWatch1, view);
                                    imgBottom.setBackgroundResource(R.drawable.custom_picture);
                                    imgTop.setBackgroundResource(R.drawable.custom_picture2);
                                } else {
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }
                                    startCountdownTimer(times, txtWatch2, view);
                                    imgTop.setBackgroundResource(R.drawable.custom_picture);
                                    imgBottom.setBackgroundResource(R.drawable.custom_picture2);
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                //reject rematch
                if (userId.equals(player1) && checkResult) {
                    if (rematchPlayer2 == 2) {
                        checkResult = false;
                        turnOffDialog();
                        showResultRematchDialog(view);
                    }
                }
                if (userId.equals(player2) && checkResult) {
                    if (rematchPlayer1 == 2) {
                        checkResult = false;
                        turnOffDialog();
                        showResultRematchDialog(view);
                    }
                }

                handler.postDelayed(this, delayTime);
            }
        }, delayTime);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Xử lý đánh cờ
//                if(adapter.isCellEmpty(position)==false&&countPlayer>0) {
//                    position=savePlayerPosition[countPlayer-1];
//                    countPlayer--;
//                }

                if (userId.equals(currentPlayer) && !player2.equals("") && checkClick && checkConnect) {
                    currentPlayerOld = currentPlayer;
                    checkClick = false;
                    onCellClicked(position, view, adapter);
                    if(!checkClick){
                        checkWatch=true;
                        positionOld = position;
                        countTurn++;
                        gameRef.child("currentPlayer").setValue(currentPlayer);
                        gameRef.child("position").setValue(position);
                        gameRef.child("countTurn").setValue(countTurn);
                        gameRef.child("gameOver").setValue(gameOver);
                    }
                }
                //savePlayerPosition[countPlayer++] = position;
            }

        });

        gridView.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private boolean isZooming = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (scaleFactor == 1) {
                    gridView.setTranslationX(xOriginal);
                    gridView.setTranslationY(yOriginal);
                }
                int pointerCount = event.getPointerCount();
                if (pointerCount > 1) {
                    isZooming = true;
                    scaleGestureDetector.onTouchEvent(event);

                } else {
                    if (scaleFactor > 1) {
                        isZooming = false;
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (!isZooming) {
                                    startX = event.getX();
                                    startY = event.getY();
                                }
                                break;

                            case MotionEvent.ACTION_MOVE:
                                if (!isZooming) {
                                    float endX = event.getX();
                                    float endY = event.getY();
                                    float dx = endX - startX;
                                    float dy = endY - startY;

                                    offsetX += dx;
                                    offsetY += dy;

                                    float[] max = caroCenter.calculateMaxOffset(gridView, scaleFactor);

                                    offsetX = Math.min(Math.max(offsetX, -max[0]), max[0]);
                                    offsetY = Math.min(Math.max(offsetY, -max[1]), max[1]);

                                    gridView.setTranslationX(offsetX);
                                    gridView.setTranslationY(offsetY);

                                    startX = endX;
                                    startY = endY;
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                if (!isZooming) {
                                    break;
                                }
                        }
                    }
                }
                return false;
            }
        });
        btnMessage = view.findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.screen_message_in_game);

                editMessage = dialog.findViewById(R.id.editMessage);
                btnSendMessage = dialog.findViewById(R.id.btnSendMessage);
                btnCancel = dialog.findViewById(R.id.btnCancel);

                btnSendMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        message = editMessage.getText().toString();
                        if (message.length() > 50) {
                            editMessage.setText("");
                            Toast.makeText(requireContext(), "You can only send messages of fewer than 50 characters", Toast.LENGTH_SHORT).show();
                        }
                        gameRef.child("message").setValue(message);
                        gameRef.child("keyMessage").setValue(userId);
                        dialog.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        imgTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInfo = new Dialog(getActivity());
                dialogInfo.setContentView(R.layout.dialog_info_ingame);

                txtNickname = dialogInfo.findViewById(R.id.nickname);
                txtNoLosses = dialogInfo.findViewById(R.id.no_losses);
                txtNoWins = dialogInfo.findViewById(R.id.no_wins);
                btnClose = dialogInfo.findViewById(R.id.btnClose);

                txtNickname.setText("Nickname: " + username);
                txtNoWins.setText("Number of wins: " + noWins);
                txtNoLosses.setText("Number of losses: " + noLosses);
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogInfo.dismiss();
                    }
                });
                dialogInfo.show();
            }
        });
        btnSurrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (winner.isEmpty()) {
                    turnOffDialog();
                    showConfirmSurrender(v);
                }
            }
        });
        btnZoom = view.findViewById(R.id.btnZoomPVPOfline);
        btnZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float newScale = 1.5f; // Scale X 2 lần
                float newScale2 = 3f; // Scale X 3 lần

                if (scaleFactor < newScale) {
                    scaleFactor = newScale; // Cập nhật scaleFactor
                } else if (scaleFactor < newScale2) {
                    scaleFactor = newScale2; // Cập nhật scaleFactor
                } else {
                    scaleFactor = 1.0f; // Trở về tỷ lệ ban đầu
                }

                // Cập nhật tỷ lệ phóng to của gridView bằng scaleFactor
                gridView.setScaleX(scaleFactor);
                gridView.setScaleY(scaleFactor);

                gridView.setTranslationX(xOriginal);
                gridView.setTranslationY(yOriginal);
            }
        });

        btnTop = view.findViewById(R.id.btnTopInGameOnline);
        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBack(v);
            }
        });
        btnHome = view.findViewById(R.id.btnHomeInGameOnline);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_inGameOnlineFragment_to_mainMenuFragment);
            }
        });
        return view;
    }

    private void readDoc() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserInfo")
                .document(userIdOpponent)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            username = documentSnapshot.getString("username");
                            Map<String, Object> data = documentSnapshot.getData();
                            if (data != null && data.containsKey("wins") && data.containsKey("losses")) {
                                noWins = documentSnapshot.getLong("wins");
                                noLosses = documentSnapshot.getLong("losses");
                            } else {
                                noWins = 0;
                                noLosses = 0;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi
                    }
                });
    }

    public void readData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
        gameRef.child("currentPlayer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentPlayerValue = snapshot.getValue(String.class);

                if (currentPlayerValue != null) {
                    currentPlayer = currentPlayerValue;
                } else {
                    currentPlayer = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });


        gameRef.child("position").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer positionValue = snapshot.getValue(Integer.class);

                if (positionValue != null) {
                    positionUpdate = positionValue.intValue();
                    if (positionUpdate > -1 && positionOld != positionUpdate && currentPlayer != currentPlayerOld && checkWatch) {
                        int row = positionUpdate / sizeBoard; // Lấy hàng dựa trên vị trí ô
                        int col = positionUpdate % sizeBoard; // Lấy cột dựa trên vị trí ô
                        if (currentPlayer.equals(player1)) {
                            board[row][col] = 2;
                            adapter.markCellAsPlayer2(positionUpdate);
                        } else {
                            board[row][col] = 1;
                            adapter.markCellAsPlayer1(positionUpdate);
                        }

                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        startCountdownTimer(times, txtWatch2, view);
                        imgTop.setBackgroundResource(R.drawable.custom_picture);
                        imgBottom.setBackgroundResource(R.drawable.custom_picture2);
                        checkWatch = false;
                        checkClick = true;
                    }
                }
                else {
                    positionUpdate=-1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });


        gameRef.child("checkRematch").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean checkDoneRematchValue = snapshot.getValue(Boolean.class);

                if (checkDoneRematchValue != null) {
                    checkDoneRematch = checkDoneRematchValue;
                } else {
                    checkDoneRematch = false;
                }

                if(checkDoneRematch){
                    progressBar.setVisibility(View.VISIBLE);
                    checkAgree=true;
                    checkDoneRematch=false;
                    gameRef.child("checkRematch").setValue(false);
                    initializeBoard(view);
                    readDoc();
                    if (userId.equals(currentPlayer)) {
                        checkClick = true;
                    } else {
                        checkClick = false;
                    }
                    if (!userId.equals(currentPlayer)) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        startCountdownTimer(times, txtWatch1, view);
                        imgBottom.setBackgroundResource(R.drawable.custom_picture);
                        imgTop.setBackgroundResource(R.drawable.custom_picture2);
                    } else {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        startCountdownTimer(times, txtWatch2, view);
                        imgTop.setBackgroundResource(R.drawable.custom_picture);
                        imgBottom.setBackgroundResource(R.drawable.custom_picture2);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("rematchPlayer1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer rematchValue = snapshot.getValue(Integer.class);

                if (rematchValue != null) {
                    rematchPlayer1 = rematchValue;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });
        gameRef.child("rematchPlayer2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer rematchValue = snapshot.getValue(Integer.class);

                if (rematchValue != null) {
                    rematchPlayer2 = rematchValue;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String messageValue = snapshot.getValue(String.class);

                if (messageValue != null) {
                    message = messageValue;
                } else {
                    // Gán giá trị mặc định hoặc xử lý khi giá trị là null
                    message = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("keyMessage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String keyMessageValue = snapshot.getValue(String.class);

                if (keyMessageValue != null) {
                    keyMessage = keyMessageValue;
                } else {
                    // Gán giá trị mặc định hoặc xử lý khi giá trị là null
                    keyMessage = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("checkRandom").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean checkRandomValue = snapshot.getValue(Boolean.class);

                if (checkRandomValue != null) {
                    checkRandom = checkRandomValue;
                }else {
                    checkRandom=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("player2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player2Value = snapshot.getValue(String.class);

                if (player2Value != null) {
                    player2 = player2Value;
                } else {
                    player2 = "";
                }

                if (countRepeat == 0 && player2.equals("")&&!checkRandom) {
                    showRoomIdDialog(view);
                    countRepeat++;
                }
                if (countRepeat == 0 && player2.equals("")&&checkRandom) {
                    showWaitPlayerJoinRoom(view);
                    countRepeat++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("player1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player1Value = snapshot.getValue(String.class);

                if (player1Value != null) {
                    player1 = player1Value;
                } else {
                    player1 = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("gameOver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean gameOverValue = snapshot.getValue(Boolean.class);

                if (gameOverValue != null) {
                    gameOver = gameOverValue;
                }else {
                    gameOver=false;
                }
                //end game
                if (gameOver && checkWin) {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    if (checkConnect) {
                        String opponentId = (userId.equals(player1)?player2:player1);
                        updateData(opponentId);
                        showWinDialog(winner, view);
                        checkClick=false;
                        checkWin = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        gameRef.child("winner").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String winnerValue = snapshot.getValue(String.class);

                if (winnerValue != null) {
                    winner = winnerValue;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });
        gameRef.child("countTurn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer countTurnValue = snapshot.getValue(Integer.class);

                if (countTurnValue != null) {
                    countTurn = countTurnValue;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });
    }

    private void updateData(String opponentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference playerRef = db.collection("UserInfo").document(userId);

        playerRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Save match history
                            UserInfo userInfo = documentSnapshot.toObject(UserInfo.class);

                            if(userInfo.getMatchHistory() == null) {
                                userInfo.setMatchHistory(new ArrayList<>());
                            }

                            if(userInfo.getMatchHistory().size() == 10) {
                                userInfo.getMatchHistory().remove(9);
                            }

                            userInfo.getMatchHistory().add(0, new MatchHistory(opponentId, new java.util.Date(), winner.equals(userId)));

                            // Save win loose count and Save coins
                            if (winner.equals(userId)) {
                                userInfo.setWins(userInfo.getWins() + 1);
                                userInfo.getCoins().setCopperCoins(userInfo.getCoins().getCopperCoins() + 20);
                            } else {
                                userInfo.setLosses(userInfo.getLosses() + 1);
                                userInfo.getCoins().setCopperCoins(userInfo.getCoins().getCopperCoins() + 5);
                            }

                            //Save userInfo to db
                            FirebaseHelper.getInstance().addDataToDatabase("UserInfo", userId, userInfo);
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi khi kiểm tra sự tồn tại của tài liệu
                    }
                });
    }

    private void initializeBoard(View v) {
        turnOffDialog();
        checkWatch = true;
        checkDoneRematch=false;
        checkClick = false;
        checkAgree=false;
        checkConnect = true;
        checkWin = true;
        checkRematch = true;
        checkResult=true;
        checkRandom=false;

        board = new int[sizeBoard][sizeBoard];  // Bảng sizeBoardxsizeBoard
        //savePlayerPosition=new int[sizeBoard*sizeBoard/2];
        // Khởi tạo tất cả ô cờ là trống
        for (int i = 0; i < sizeBoard; i++) {
            for (int j = 0; j < sizeBoard; j++) {
                board[i][j] = 0;
                int p = i * sizeBoard + j;
                adapter.markCellAsPlayer0(p);
            }
        }
        //set dong ho
        if (times != -1) {
            // Cập nhật TextView với thời gian còn lại
            long seconds = times / 1000 - 1;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            txtWatch2.setText(String.format("%02d:%02d", minutes, seconds));
            txtWatch1.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    // Hàm xử lý sự kiện khi người chơi đánh vào một ô cờ
    public void onCellClicked(int position, View v, AdapterGridview adapter) {
        if (!gameOver) {
            int row = position / sizeBoard; // Lấy hàng dựa trên vị trí ô
            int col = position % sizeBoard; // Lấy cột dựa trên vị trí ô
            // Kiểm tra nếu ô đã được đánh
            if (adapter.isCellEmpty(position)) {
                // Đánh dấu ô và thay đổi hình ảnh
                if (player1.equals(currentPlayer)) {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    board[row][col] = 1;
                    adapter.markCellAsPlayer1(position);
//                    adapter.markCellBackground1(position);
//                    if(countPlayer>0) {
//                        adapter.markCellAsPlayer2(savePlayerPosition[countPlayer - 1]);
//                    }
                } else {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    board[row][col] = 2;
                    adapter.markCellAsPlayer2(position);
//                    adapter.markCellBackground2(position);
//                    if(countPlayer>0) {
//                        adapter.markCellAsPlayer1(savePlayerPosition[countPlayer - 1]);
//                    }
                }

                // Kiểm tra thắng
                int checkPlayer;
                if (currentPlayer.equals(player1)) {
                    checkPlayer = 1;
                } else {
                    checkPlayer = 2;
                }
                if (caroCenter.checkWin(checkPlayer, sizeBoard, board)) {
                    gameOver = true;
                    checkClick=false;
                    gameRef.child("winner").setValue(currentPlayer);
                }
                //dung o luot choi cua nguoi win
                if (!gameOver) {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    startCountdownTimer(times, txtWatch1, v);
                    imgBottom.setBackgroundResource(R.drawable.custom_picture);
                    imgTop.setBackgroundResource(R.drawable.custom_picture2);
                }
                if (currentPlayer.equals(player1)) {
                    currentPlayer = player2;
                } else {
                    currentPlayer = player1;
                }
            } else {
                checkClick=true;
                Toast.makeText(requireContext(), "Cell already marked", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void showWinDialog(String player, View v) {
        if (v.isShown()) {
            String title, message;
            if (player.equals(userId)) {
                message = "Keep playing even better.";
                title = "You won!";
            } else {
                message = "No worries, try harder next time.";
                title = "Your opponent won!";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(false);
            builder.setPositiveButton("Rematch", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (userId.equals(player1)) {
                        gameRef.child("rematchPlayer1").setValue(1);
                    } else {
                        gameRef.child("rematchPlayer2").setValue(1);
                    }
                    showWaitRematchDialog(v);
                }
            });
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            winnerDialog = builder.create();
            winnerDialog.show();
        }
    }
    private void showWaitRematchDialog(View v) {
        if (v.isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Rematch");
            builder.setMessage("Wait for the opponent to agree");
            builder.setCancelable(false);
            builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onClickBack(v);
                }
            });
            waitRematchDialog = builder.create();
            waitRematchDialog.show();
        }
    }
    private void showResultRematchDialog(View v) {
        if (v.isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Rematch");
            builder.setMessage("The opponent does not want to continue the match");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onClickBack(v);
                }
            });
            resultRematchDialog = builder.create();
            resultRematchDialog.show();
        }
    }
    private void showRematchDialog(View v) {
        if (v.isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Rematch");
            builder.setMessage("The opponent wants a rematch");
            builder.setCancelable(false);
            builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (userId.equals(player1)) {
                        gameRef.child("rematchPlayer1").setValue(1);
                    } else {
                        gameRef.child("rematchPlayer2").setValue(1);
                    }
                }
            });
            builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (userId.equals(player1)) {
                        gameRef.child("rematchPlayer1").setValue(2);
                    } else {
                        gameRef.child("rematchPlayer2").setValue(2);
                    }
                }
            });
            rematchDialog = builder.create();
            rematchDialog.show();
        }
    }

    private void showNoInternetDialog(View v) {
        if (v.isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("No Internet Connection");
            builder.setMessage("Please check your internet connection and try again.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Home", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_inGameOnlineFragment_to_mainMenuFragment);
                }
            });
            noInternetDialog = builder.create();
            noInternetDialog.show();
        }
    }
    private void showConfirmSurrender(View v) {
        if (v.isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Confirm surrender");
            builder.setMessage("You will face defeat.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (userId.equals(player1)) {
                        gameRef.child("winner").setValue(player2);
                        gameRef.child("gameOver").setValue(true);

                    } else {
                        gameRef.child("winner").setValue(player1);
                        gameRef.child("gameOver").setValue(true);

                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            confirmSurrender = builder.create();
            confirmSurrender.show();
        }
    }

    private void showRoomIdDialog(View v) {
        if (v.isShown()) {
            if (!player2.isEmpty()) {
                return; // Nếu player2 có giá trị, không làm gì cả và thoát
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("roomId is " + idRoom);
            builder.setMessage("Give it to the friend you want to play with");
            builder.setCancelable(false);
            builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onClickBack(v);
                }
            });
            roomIdDialog = builder.show();
        }
    }
    private void showWaitPlayerJoinRoom(View v) {
        if (v.isShown()) {
            if (!player2.isEmpty()) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Wait for other players");
            builder.setMessage("Wait for the second player to start the match");
            builder.setCancelable(false);
            builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onClickBack(v);
                }
            });
            roomIdDialog = builder.show();
        }
    }

    private void startCountdownTimer(long millisInFuture, TextView txtWatch, View v) {
        // millisInFuture là thời gian đếm ngược theo mili giây

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Cập nhật TextView với thời gian còn lại
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                txtWatch.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                // Xử lý khi đếm ngược kết thúc (hết thời gian)
                if (times != -1) {
                    if (txtWatch.getText().toString().equals("00:00") && checkConnect) {
                        gameOver = true;
                        checkClick=false;
                    }
                    if (currentPlayer.equals(player1)) {
                        currentPlayer = player2;
                    } else {
                        currentPlayer = player1;
                    }
                    gameRef.child("winner").setValue(currentPlayer);
                    gameRef.child("gameOver").setValue(gameOver);
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Dừng đồng hồ đếm khi Activity bị hủy để tránh rò rỉ bộ nhớ
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    float xOriginal, yOriginal;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

            ObjectAnimator scaleAnimatorX = ObjectAnimator.ofFloat(gridView, "scaleX", scaleFactor);
            ObjectAnimator scaleAnimatorY = ObjectAnimator.ofFloat(gridView, "scaleY", scaleFactor);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleAnimatorX, scaleAnimatorY);

            int duration = 1000;
            if (scaleFactor == 1) {
                duration = 0;
            }
            animatorSet.setDuration(duration);
            animatorSet.start();

            return true;
        }
    }

    public void onClickBack(View v) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        NavController navController = Navigation.findNavController(v);
        navController.navigate(R.id.action_inGameOnlineFragment_to_pvpFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        handler.removeCallbacksAndMessages(null);
        
        if(rematchPlayer1!=1||rematchPlayer2!=1) {
            if (userId.equals(player1)) {
                gameRef.child("rematchPlayer1").setValue(2, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(rematchPlayer2==2&&rematchPlayer1==2 || player2.equals("")){
                            gameRef.removeValue();
                        }
                    }
                });
            } else {
                gameRef.child("rematchPlayer2").setValue(2, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(rematchPlayer2==2&&rematchPlayer1==2||player2.equals("")){
                            gameRef.removeValue();
                        }
                    }
                });
            }
        }

        turnOffDialog();
    }

    public void turnOffDialog() {
        if (winnerDialog != null && winnerDialog.isShowing()) {
            winnerDialog.dismiss();
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (dialogInfo != null && dialogInfo.isShowing()) {
            dialogInfo.dismiss();
        }
        if (roomIdDialog != null && roomIdDialog.isShowing()) {
            roomIdDialog.dismiss();
        }
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
        }
        if (waitRematchDialog != null && waitRematchDialog.isShowing()) {
            waitRematchDialog.dismiss();
        }
        if (rematchDialog != null && rematchDialog.isShowing()) {
            rematchDialog.dismiss();
        }
        if (resultRematchDialog != null && resultRematchDialog.isShowing()) {
            resultRematchDialog.dismiss();
        }
        if (confirmSurrender != null && confirmSurrender.isShowing()) {
            confirmSurrender.dismiss();
        }
    }


}

