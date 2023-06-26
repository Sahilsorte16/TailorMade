package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShopDetails extends AppCompatActivity {


    AutoCompleteTextView autoCompleteTextView;
    EditText pinCode, stateEdit, DistrictEdit, LocalityEdit, shopName;
    Button loc;
    Spinner country;
    Geocoder geocoder;
    LocationManager locationManager;
    ImageView profile, Continue;
    TextView naame;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    Uri selectedImageUri;
    Map<String,Object> Shop;
    String Uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopName = findViewById(R.id.shop);
        pinCode = (EditText) findViewById(R.id.pin);
        stateEdit = findViewById(R.id.states);
        DistrictEdit = findViewById(R.id.district);
        LocalityEdit = findViewById(R.id.streetName);
        loc = findViewById(R.id.location);
        profile = findViewById(R.id.profilePic);
        Continue = findViewById(R.id.Continue);
        naame = findViewById(R.id.naam);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        selectedImageUri = null;
        Intent in = getIntent();

        String name = in.getStringExtra("Name");
        String email = in.getStringExtra("Email");
        Uid = in.getStringExtra("Hash");
        naame.setText("Hi, " + name);
        pinCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    String pin_code = pinCode.getText().toString();
                    showLoadingDialog();
                    fetchLocationData(pin_code);

                }
            }
        });


        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getCurrentLocation();

            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 100);
            }
        });

        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shop = shopName.getText().toString();
                String PIN = pinCode.getText().toString();
                String street = LocalityEdit.getText().toString();
                String City = DistrictEdit.getText().toString();
                String state = stateEdit.getText().toString();
                String country = "India";
                if(shop.isEmpty() || PIN.isEmpty() || street.isEmpty() || City.isEmpty() || state.isEmpty() || country.isEmpty())
                {
                    Toast.makeText(ShopDetails.this, "All details are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    Shop = new HashMap<>();
                    Shop.put("Owner", name);
                    Shop.put("Email",email);
                    Shop.put("shopName", shop);
                    List<Object> address= new ArrayList<>(Arrays.asList(street, City, state, country, PIN));
                    Shop.put("Stitch for", "Female");
                    Shop.put("Address", address);

                    if(selectedImageUri!=null)
                        storeImagetoStorage(selectedImageUri);
                    else
                        storeToFirestore(Shop);
                    startActivity(new Intent(ShopDetails.this, HomePage.class));
                }
            }
        });


        autoCompleteTextView = findViewById(R.id.states);
        country = findViewById(R.id.spinner);
        String[] states = {"Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
                "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
                "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
                "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
                "Uttar Pradesh", "Uttarakhand", "West Bengal", "Andaman and Nicobar Islands",
                "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Ladakh",
                "Lakshadweep", "Puducherry"};
        ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, states);

        String[] India = {"India"};
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, India);

        autoCompleteTextView.setAdapter(statesAdapter);
        country.setAdapter(countryAdapter);

    }

    private void storeImagetoStorage(Uri selectedImageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(Uid+"/profile.jpg");
        UploadTask uploadTask = imageRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Uri imageUrl = uri;
                Shop.put("Image", imageUrl);
                storeToFirestore(Shop);
            });
        });
    }

    private void storeToFirestore(Map<String, Object> shop) {

        DocumentReference documentRef = db.collection("Shop").document(Uid);
        documentRef.set(shop).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(ShopDetails.this, "Shop set up failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLocationData(String pinCode) {
        if (pinCode.length() != 6) {
            Toast.makeText(this, "Invalid Pin Code", Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        String str = "{\r\"searchBy\": \"pincode\",\r\"value\": " + pinCode + "\r}";
        RequestBody body = RequestBody.create(mediaType, str);
        Request request = new Request.Builder()
                .url("https://pincode.p.rapidapi.com/")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-RapidAPI-Key", "726ca9362emsh492caead68db326p151a1fjsn5903e76676d0")
                .addHeader("X-RapidAPI-Host", "pincode.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(ShopDetails.this, "Invalid PIN code", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful())
                    return;

                String responseBody = response.body().string();

                try {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    if(jsonArray.length()>0)
                    {
                        String state = jsonArray.getJSONObject(0).get("circle").toString();
                        String district = jsonArray.getJSONObject(0).get("district").toString();

                        stateEdit.setText(state);
                        DistrictEdit.setText(district);
                    }

                } catch (JSONException e) {
                    //Toast.makeText(ShopDetails.this, "Invalid PIN code", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                dismissLoadingDialog();
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            // Permission denied, handle accordingly (e.g., show an error message).
        }
    }

    private void getCurrentLocation() {
        // Create a LocationManager instance
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestLocationPermission();

            return;
        }
        showLoadingDialog();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Use the latitude and longitude to get the address
                getAddressFromLocation(latitude, longitude);

                // Stop listening for location updates
                locationManager.removeUpdates(this);
            }
        });

    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Retrieve desired address details (e.g., street, city, country)
                String street = getLocality(address.getAddressLine(0));
                String city = address.getLocality();
                String state = address.getAdminArea();
                String pin = address.getPostalCode();

                LocalityEdit.setText(street);
                DistrictEdit.setText(city);
                stateEdit.setText(state);
                pinCode.setText(pin);

                System.out.println("+++++++++++++++++++++++++");
                System.out.println(street + " " + city + " " + state + " " + country);
                System.out.println(address);
                System.out.println("+++++++++++++++++++++++++");

                dismissLoadingDialog();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, proceed with location retrieval
            getCurrentLocation();
        }
    }

    private String getLocality(String address)
    {
        String[] components = address.split(", ");

        StringBuilder newAddress = new StringBuilder();

        // Append the desired components except the last three
        for (int i = 0; i < components.length - 3; i++) {
            newAddress.append(components[i]);

            if (i < components.length - 4) {
                newAddress.append(", ");
            }
        }

        return newAddress.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile);
        }

    }


    private ProgressDialog progressDialog;

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private static String generateUniqueID(String phoneNumber) {
        String hash = phoneNumber;

        try {
            // Create an instance of the MD5 hashing algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convert the phone number to bytes and generate the hash
            md.update(phoneNumber.getBytes());
            byte[] digest = md.digest();

            // Convert the byte array to a BigInteger
            BigInteger bigInt = new BigInteger(1, digest);

            // Convert the BigInteger to a hexadecimal string
            hash = bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // Handle exceptions related to the hashing algorithm
            e.printStackTrace();
        }

        return hash;
    }

    @Override
    public void onBackPressed() {}
}