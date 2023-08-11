package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Add_Item extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private static final int REQUEST_IMAGE = 134;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    String[] suggestions = {
            "Patiala Salwar", "Anarkali Suit", "Churidar", "Palazzo Pants", "Lehenga Choli",
            "A-Line Skirt", "Blouse", "Salwar Kameez", "Straight Pants", "Sharara",
            "Dhoti Pants", "Draped Saree", "Ghagra Choli", "Jacket Lehenga", "Kalidar Suit",
            "Kaftan Dress", "Kurta", "Kurti", "Lengha Saree", "Pant Saree",
            "Punjabi Suit", "Salwar", "Saree", "Skirt", "Tunic",
            "Crop Top", "Front Slit Kurti", "Indo-Western Dress", "Cigarette Pants", "Jumpsuit",
            "Pakistani Suit", "Patiala Suit", "Poncho", "Sherwani", "Trail Gown",
            "Tulip Pants", "Cape", "Gown", "Peplum Top", "Maxi Dress",
            "Off-Shoulder Dress", "Patiala", "Printed Saree", "Sari", "Semi-Stitched Lehenga",
            "Shirt", "Silk Saree", "Tunic Dress", "Wrap Around Skirt", "Embroidered Blouse"
    };
    int pos = -1;
    AutoCompleteTextView itemName;
    Spinner type;
    EditText charges, instruction, quantity;
    TextView chargesInText, header, totalCharges;
    Button bodyMeasurements, save;
    ImageView addInstr, mic, addCloth, addPattern, back;
    RecyclerView rv, rv1, rv2;
    String ImageType = "Cloth ", Cid, item_type="Stitching";
    List<String> instructions;
    List<Bitmap> ClothImages, PatternImages, DressImages;
    Map<String,String> bodyMs;
    Boolean complete;
    private String expenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        itemName = findViewById(R.id.itemName);
        type = findViewById(R.id.Spinner);
        charges = findViewById(R.id.editTextCharges);
        chargesInText = findViewById(R.id.Charges_in_text);
        bodyMeasurements = findViewById(R.id.body_measurements);
        quantity = findViewById(R.id.editTextQuantity);
        totalCharges = findViewById(R.id.totalCharges);
        back = findViewById(R.id.back);
        addInstr = findViewById(R.id.addInstr);
        addCloth = findViewById(R.id.addClothImage);
        addPattern = findViewById(R.id.addPatternImage);
        header = findViewById(R.id.textView6);
        save = findViewById(R.id.saveItem);
        rv = findViewById(R.id.recyclerView);
        rv1 = findViewById(R.id.recyclerView1);
        rv2 = findViewById(R.id.recyclerView2);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                item_type = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        instructions = new ArrayList<>();
        ClothImages = new ArrayList<>();
        PatternImages = new ArrayList<>();
        bodyMs = new HashMap<>();

        Intent in = getIntent();
        Cid = in.getStringExtra("Cid");
        header.setText(in.getStringExtra("activity"));
        pos = in.getIntExtra("LayoutPosition",-1);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        itemName.setAdapter(adapter);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                computeCharges(quantity.getText().toString(), charges.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        charges.addTextChangedListener(textWatcher);
        quantity.addTextChangedListener(textWatcher);

        addInstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Add_Item.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_instruction, null);
                instruction = dialogView.findViewById(R.id.instruction);
                mic = dialogView.findViewById(R.id.mic);

                mic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        promptSpeechInput();
                    }
                });

                builder.setView(dialogView)
                    .setTitle("Add Instruction")
                    .setIcon(R.drawable.baseline_add_circle_24)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String str = instruction.getText().toString();
                            if(str.isEmpty())
                            {
                                Toast.makeText(Add_Item.this, "The instruction is blank", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            instructions.add(str);
                            InstructionsAdapter instructionsAdapter = new InstructionsAdapter(instructions, Add_Item.this);
                            rv.setAdapter(instructionsAdapter);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
            }
        });

        addCloth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageType = "Cloth ";
                clickPicture();
            }
        });

        addPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageType = "Pattern ";
                clickPicture();
            }
        });

        bodyMeasurements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Add_Item.this, SelectBodyMeasurements.class);
                intent.putExtra("Cid", Cid);
                intent.putExtra("selectedBodyMs", (Serializable) bodyMs);
                startActivityForResult(intent, 8);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimatorSet animatorSet = Animations.backAnimation(back);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        onBackPressed();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {

                    }
                });
                animatorSet.start();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendItemBack();
            }
        });

        if(in.getStringExtra("activity").equals("Edit Item"))
        {
            itemName.setText(in.getStringExtra("Item Name"));
            if(in.getStringExtra("Item type").equals("Stitching"))
                type.setSelection(0);
            else
                type.setSelection(1);
            charges.setText(in.getStringExtra("Charges"));
            quantity.setText(in.getStringExtra("Quantity"));
            Gson gson = new Gson();
            expenses = in.getStringExtra("Expenses");
            instructions = in.getStringArrayListExtra("Instructions");
            complete = in.getBooleanExtra("isComplete", false);
            ClothImages =  byteToBitmap((ArrayList<byte[]>) in.getSerializableExtra("Cloth Images"));
            PatternImages = byteToBitmap((ArrayList<byte[]>) in.getSerializableExtra("Pattern Images"));
            DressImages = byteToBitmap((ArrayList<byte[]>) in.getSerializableExtra("Dress Images"));

            bodyMs = (Map<String, String>) in.getSerializableExtra("Body Measurements");

            InstructionsAdapter instructionsAdapter = new InstructionsAdapter(instructions, Add_Item.this);
            rv.setAdapter(instructionsAdapter);

            ImageAdaptor imageAdaptor = new ImageAdaptor(ClothImages, Add_Item.this, "Cloth ");
            rv1.setAdapter(imageAdaptor);

            ImageAdaptor imageAdaptor1 = new ImageAdaptor(PatternImages, Add_Item.this, "Pattern ");
            rv2.setAdapter(imageAdaptor1);

        }

    }

    public void computeCharges(String quantity, String rate)
    {
        String t = "Total Item Charges:    <b> \u20b9 ";
        int q=0, r=0;
        if(!quantity.isEmpty())
            q = Integer.parseInt(quantity);
        if(!rate.isEmpty())
            r = Integer.parseInt(rate);

        String ttlCharges = String.valueOf(q*r);
        totalCharges.setText(Html.fromHtml(t + ttlCharges + "</b>"));

        if (!ttlCharges.equals("0")) {
            try {
                int charges = Integer.parseInt(ttlCharges);
                RupeesConverter rupeesConverter = new RupeesConverter();
                String result = rupeesConverter.convertToIndianRupeesWords(charges);
                chargesInText.setText(result);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            chargesInText.setText("");
        }
    }

    private List<Bitmap> byteToBitmap(ArrayList<byte[]> imageBytesList) {
        List<Bitmap> bitmapList = new ArrayList<>();

        if(imageBytesList!=null)
        {
            for (byte[] imageBytes : imageBytesList) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                bitmapList.add(bitmap);
            }
        }
        return bitmapList;
    }

    private void sendItemBack() {
        String item_name = itemName.getText().toString();
        String item_charges = charges.getText().toString();
        String item_quantity = quantity.getText().toString();
        if(item_name.isEmpty())
        {
            Toast.makeText(this, "Item Name missing", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(item_charges.isEmpty() || item_quantity.isEmpty())
        {
            Toast.makeText(this, "Check charges and rate for the item", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(bodyMs.isEmpty())
        {
            Toast.makeText(this, "Choose body measurements", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(ClothImages.isEmpty() && PatternImages.isEmpty())
        {
            Toast.makeText(this, "At least one image required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("Item Name", item_name);
        intent.putExtra("Item type", item_type);
        intent.putExtra("Body Measurements", (Serializable) bodyMs);
        intent.putExtra("isComplete", complete);
        intent.putExtra("Charges", item_charges);
        intent.putExtra("Quantity", quantity.getText().toString());
        intent.putExtra("Expenses", expenses);
        intent.putExtra("Total amount", String.valueOf(Integer.parseInt(item_charges)*Integer.parseInt(quantity.getText().toString())));
        intent.putStringArrayListExtra("Instructions", (ArrayList<String>) instructions);
        intent.putExtra("Cloth Images", convertToByteArray(ClothImages));
        intent.putExtra("Pattern Images", convertToByteArray(PatternImages));
        intent.putExtra("Dress Images", convertToByteArray(DressImages));
        intent.putExtra("LayoutPosition", pos);
        setResult(RESULT_OK, intent);
        finish();
    }

    private ArrayList<byte[]> convertToByteArray(List<Bitmap> bitmapList) {
        ArrayList<byte[]> byteArrayArrayList = new ArrayList<>();
        if(bitmapList!=null)
        {
            for (Bitmap bitmap : bitmapList) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayArrayList.add(byteArray);
            }
        }
        return byteArrayArrayList;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save changes")
                .setMessage("Do you want to save the changes?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendItemBack();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED,intent);
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goBack() {
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
    }

    private void clickPicture() {
        if (ContextCompat.checkSelfPermission(Add_Item.this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(Add_Item.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }


    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void promptSpeechInput() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_SPEECH_INPUT);
        } else {
            startSpeechToText();
        }
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Sorry, speech recognition is not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    instruction.setText(spokenText);
                }
            }
        }
        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if(imageBitmap != null)
                {
                    Bitmap croppedBitmap = cropToSquare(imageBitmap);
                    if(ImageType.equals("Cloth "))
                    {
                        ClothImages.add(croppedBitmap);
                        ImageAdaptor imageAdaptor = new ImageAdaptor(ClothImages, Add_Item.this, ImageType);
                        rv1.setAdapter(imageAdaptor);
                    }
                    else if (ImageType.equals("Pattern ")) {
                        PatternImages.add(croppedBitmap);
                        ImageAdaptor imageAdaptor = new ImageAdaptor(PatternImages, Add_Item.this, ImageType);
                        rv2.setAdapter(imageAdaptor);
                    }
                }

            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }

        }
        else if(requestCode==8 && resultCode == RESULT_OK && data != null)
        {
            bodyMs = (Map<String, String>)data.getSerializableExtra("selectedBodyMs");
        }
    }

    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        return Bitmap.createBitmap(bitmap, x, y, size, size);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechToText();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
                openCamera();
            } else {
                // Camera permission denied
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //
//    private AlertDialog recordingDialog;
//    private TextView txtTimer;
//    private ImageView btnStop;
//    private boolean isRecording = false;
//    private long timerInterval = 1000; // 1 second interval
//    private long elapsedTime = 0;
//    private boolean isTimerRunning = false;
//    private Handler timerHandler;
//    private Runnable timerRunnable;
//    private static final int REQUEST_PERMISSION_CODE = 200;
//    private MediaRecorder mediaRecorder;
//    private String audioFilePath;
//
//
//    private void showRecordingDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_instruction.xml, null);
//        builder.setView(dialogView)
//                .setTitle("Record Instructions")
//                .setIcon(R.drawable.baseline_mic_24)
//                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        stopTimer();
//                    }
//                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        stopTimer();
//                    }
//                });
//
//
//        txtTimer = dialogView.findViewById(R.id.timer);
//        btnStop = dialogView.findViewById(R.id.button);
//        recordingDialog = builder.create();
//        recordingDialog.show();
//
//        btnStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                checkPermissionsAndStartRecording();
//            }
//        });
//    }
//
//    private void toggleRecording() {
//        if (!isRecording) {
//            //startRecording();
//            startRecording();
//            btnStop.setImageResource(R.drawable.baseline_pause_24);
//        } else {
//            //pauseRecording();
//            btnStop.setImageResource(R.drawable.baseline_play_arrow_24);
//            pauseTimer();
//        }
//        isRecording = !isRecording;
//    }
//    private void startTimer() {
//        if (!isTimerRunning) {
//            isTimerRunning = true;
//            timerHandler = new Handler();
//            timerRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    elapsedTime += timerInterval;
//                    updateTimerText(elapsedTime);
//                    timerHandler.postDelayed(this, timerInterval);
//                }
//            };
//            timerHandler.postDelayed(timerRunnable, timerInterval);
//        }
//    }
//
//    private void pauseTimer() {
//        if (isTimerRunning) {
//            isTimerRunning = false;
//            timerHandler.removeCallbacks(timerRunnable);
//        }
//    }
//
//    private void stopTimer() {
//        if (isTimerRunning) {
//            isTimerRunning = false;
//            timerHandler.removeCallbacks(timerRunnable);
//            timerHandler = null;
//            timerRunnable = null;
//            elapsedTime = 0;
//        }
//    }
//
//
//    private void updateTimerText(long elapsedTime) {
//        long seconds = elapsedTime / 1000;
//        txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
//    }
//
//    private void checkPermissionsAndStartRecording() {
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
//        } else {
//            toggleRecording();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                toggleRecording();
//            } else {
//                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void startRecording() {
//        String audioFileName = "recording.3gp";
//
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//        File directory = getFilesDir();
//        File audioFile = new File(directory, audioFileName);
//        audioFilePath = audioFile.getAbsolutePath();
//        System.out.println("*************************" + audioFilePath);
//        mediaRecorder.setOutputFile(audioFilePath);
//
//        try {
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//            startTimer();
//            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//
//
//
//











    public static class RupeesConverter {
        private final String[] units = {
                "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
        };

        private final String[] tens = {
                "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
        };

        private String convertLessThanOneThousand(int number) {
            String current;

            if (number % 100 < 20) {
                current = units[number % 100];
                number /= 100;
            } else {
                current = units[number % 10];
                number /= 10;

                current = tens[number % 10] + " " +current;
                number /= 10;
            }
            if (number == 0) return current;
            return units[number] + " hundred" + ((current.isEmpty()) ? "" : " and " + current);
        }

        public String convertToIndianRupeesWords(int number) {
            if (number == 0) {
                return "zero rupees";
            }

            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(0);

            String amountInWords = "";

            try {
                long rupees = Long.parseLong(df.format(number));

                if (rupees < 0 || rupees > 999999999) {
                    return "Illegal Amount";
                }

                int crore = (int) (rupees / 10000000);
                int lakh = (int) ((rupees / 100000) % 100);
                int thousand = (int) ((rupees / 1000) % 100);
                int remainder = (int) (rupees % 1000);

                if (crore > 0) {
                    amountInWords += convertLessThanOneThousand(crore) + " crore ";
                }

                if (lakh > 0) {
                    amountInWords += convertLessThanOneThousand(lakh) + " lakh ";
                }

                if (thousand > 0) {
                    amountInWords += convertLessThanOneThousand(thousand) + " thousand ";
                }

                if (remainder > 0) {
                    amountInWords += convertLessThanOneThousand(remainder);
                }

                amountInWords += " rupees only";
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return amountInWords;
        }
    }
}