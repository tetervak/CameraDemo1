package ca.javateacher.camerademo1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import java.io.File;
import java.util.List;

public class PictureFragment extends Fragment {

  private static final int REQUEST_PHOTO = 2;
  private static final String mFileName = "picture";
  private File mPhotoFile;
  private ImageView mPhotoView;
  private Context mAppContext;

  public PictureFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_picture, container, false);

    mAppContext = view.getContext().getApplicationContext();

    File filesDir = mAppContext.getFilesDir();
    mPhotoFile = new File(filesDir, mFileName);

    PackageManager packageManager = mAppContext.getPackageManager();

    ImageButton cameraButton = view.findViewById(R.id.camera_button);
    final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    boolean canTakePhoto = mPhotoFile != null &&
      captureIntent.resolveActivity(packageManager) != null;
    cameraButton.setEnabled(canTakePhoto);

    cameraButton.setOnClickListener(v -> getPicture(captureIntent));

    mPhotoView = view.findViewById(R.id.photo_view);

    updatePhotoView();

    return view;
  }

  private void getPicture(Intent captureIntent) {
    Uri uri = FileProvider.getUriForFile(mAppContext,
      "ca.javateacher.camerademo1.fileprovider", mPhotoFile);
    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

    List<ResolveInfo> cameraActivities
      = mAppContext.getPackageManager().queryIntentActivities(captureIntent,
        PackageManager.MATCH_DEFAULT_ONLY);

    for (ResolveInfo activity : cameraActivities) {
      mAppContext.grantUriPermission(activity.activityInfo.packageName,
        uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    startActivityForResult(captureIntent, REQUEST_PHOTO);
  }

  private void updatePhotoView() {
    if (mPhotoFile == null || !mPhotoFile.exists()) {
      mPhotoView.setImageDrawable(null);
      mPhotoView.setContentDescription(
        mAppContext.getString(R.string.no_image_captured));
    } else {
      Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
      mPhotoView.setImageBitmap(bitmap);
      mPhotoView.setContentDescription(
        mAppContext.getString(R.string.captured_image));
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

   if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PHOTO) {
      Uri uri = FileProvider.getUriForFile(mAppContext,
        "ca.javateacher.camerademo1.fileprovider", mPhotoFile);

      mAppContext.revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

      updatePhotoView();
    }
  }

}
