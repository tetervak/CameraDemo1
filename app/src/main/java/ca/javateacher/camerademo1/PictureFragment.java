package ca.javateacher.camerademo1;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PictureFragment extends Fragment {

  private static final int REQUEST_PHOTO = 2;

  private static final String mFileName = "picture";
  private File mPhotoFile;

  private ImageButton mCameraButton;
  private ImageView mPhotoView;

  public PictureFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_picture, container, false);

    File filesDir = getContext().getFilesDir();
    mPhotoFile = new File(filesDir, mFileName);

    PackageManager packageManager = getActivity().getPackageManager();

    mCameraButton = view.findViewById(R.id.camera_button);
    final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    boolean canTakePhoto = mPhotoFile != null &&
      captureIntent.resolveActivity(packageManager) != null;
    mCameraButton.setEnabled(canTakePhoto);

    mCameraButton.setOnClickListener(v -> getPicture(captureIntent));

    mPhotoView = view.findViewById(R.id.photo_view);
    updatePhotoView();

    return view;
  }

  private void getPicture(Intent captureImage) {
    Uri uri = FileProvider.getUriForFile(getActivity(),
      "ca.javateacher.camerademo1.fileprovider", mPhotoFile);
    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

    List<ResolveInfo> cameraActivities = getActivity()
      .getPackageManager().queryIntentActivities(captureImage,
        PackageManager.MATCH_DEFAULT_ONLY);

    for (ResolveInfo activity : cameraActivities) {
      getActivity().grantUriPermission(activity.activityInfo.packageName,
        uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    startActivityForResult(captureImage, REQUEST_PHOTO);
  }

  private void updatePhotoView() {
    if (mPhotoFile == null || !mPhotoFile.exists()) {
      mPhotoView.setImageDrawable(null);
      mPhotoView.setContentDescription("no image");
    } else {
      Bitmap bitmap = PictureUtils.getScaledBitmap(
        mPhotoFile.getPath(), getActivity());
      mPhotoView.setImageBitmap(bitmap);
      mPhotoView.setContentDescription("captured image");
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

   if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PHOTO) {
      Uri uri = FileProvider.getUriForFile(getActivity(),
        "ca.javateacher.camerademo1.fileprovider", mPhotoFile);

      getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

      updatePhotoView();
    }
  }

}
