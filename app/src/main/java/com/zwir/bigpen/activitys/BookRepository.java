package com.zwir.bigpen.activitys;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zwir.bigpen.R;
import com.zwir.bigpen.adapters.BookStoreAdapter;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.fragments.AccountFragment;
import com.zwir.bigpen.fragments.BookStoreFragment;
import com.zwir.bigpen.fragments.CartFragment;
import com.zwir.bigpen.fragments.LogInOrCreateFragment;
import com.zwir.bigpen.fragments.ProjectsFragment;
import com.zwir.bigpen.fragments.PurchasedBooksFragment;
import com.zwir.bigpen.util.GlobalMethods;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;

import static com.zwir.bigpen.util.FirebaseUtil.fireBaseUsers;

public class BookRepository extends AppCompatActivity {

    ImageButton lastButton,btnBookRepositoryFrag,btnMyPurchaseFrag,btnMyProjectsFrag,btnMyCartFrag,btnAccountFrag;
    TextView cartSize;
    SearchView bookSearchView;
    SearchView.SearchAutoComplete searchAutoComplete;
    FloatingActionButton fabSearch;
    BookStoreAdapter bookStoreAdapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference cartRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_repository);
        mAuth = FirebaseAuth.getInstance();
        lastButton=btnBookRepositoryFrag=findViewById(R.id.btn_book_store_frg);
        btnMyPurchaseFrag=findViewById(R.id.btn_my_purchase_frg);
        btnMyProjectsFrag=findViewById(R.id.btn_my_projects_frg);
        btnMyCartFrag=findViewById(R.id.btn_my_cart_frg);
        btnAccountFrag=findViewById(R.id.btn_account_frg);
        cartSize=findViewById(R.id.cart_size);
        btnBookRepositoryFrag.setOnClickListener(navigationButtonListener);
        btnMyPurchaseFrag.setOnClickListener(navigationButtonListener);
        btnMyProjectsFrag.setOnClickListener(navigationButtonListener);
        btnMyCartFrag.setOnClickListener(navigationButtonListener);
        btnAccountFrag.setOnClickListener(navigationButtonListener);
        bookSearchView = findViewById(R.id.book_search_view);
        fabSearch=findViewById(R.id.fab_search);
        fabSearch.setOnClickListener(navigationButtonListener);
        searchAutoComplete = bookSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(getResources().getColor( R.color.colorPrimaryDark));
        searchAutoComplete.setTextColor(getResources().getColor( R.color.white));
        searchAutoComplete.setDropDownBackgroundResource( R.color.transparent);
        myBookStoreFragment();
        bookSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                bookStoreAdapter.getFilter().filter(newText);
                return true;
            }
        });
        bookSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                bookSearchView.setVisibility(View.GONE);
                return false;
            }
        });
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                bookSearchView.setQuery(queryString,false);
                dismissKeyboard(searchAutoComplete);
            }
        });
        // active listen to user logged in or not.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user==null){
                    myBookStoreFragment();
                    cartSize.setVisibility(View.GONE);
                    if (cartRef!=null)
                        cartRef.removeEventListener(cartValueEventListener);
                }
                else {
                    cartRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(Constants.fireBaseCartsRef);
                    cartRef.addValueEventListener(cartValueEventListener);

                }
            }
        };
    }
    private ValueEventListener cartValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                cartSize.setText(String.valueOf(dataSnapshot.getChildrenCount()) );
                cartSize.setVisibility(View.VISIBLE);
            }
            else{
                cartSize.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            cartSize.setVisibility(View.GONE);
        }
    };
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    View.OnClickListener navigationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() != lastButton.getId()) {
                switch (v.getId()) {
                    case R.id.btn_book_store_frg:
                        myBookStoreFragment();
                        break;
                    case R.id.btn_my_purchase_frg:
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            myPurchaseFragment();
                        } else {
                            myLogInOrCreateFragment(Constants.toPurchaseFrag);
                        }
                        break;
                    case R.id.btn_my_projects_frg:
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            myProjectsFragment();
                        } else {
                            myLogInOrCreateFragment(Constants.toProjectFrag);
                        }
                        break;
                    case R.id.btn_my_cart_frg:
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            myCartFragment();
                        } else {
                            myLogInOrCreateFragment(Constants.toCartFrag);
                        }
                        break;
                    case R.id.btn_account_frg:
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            myAccountFragment();
                        } else {
                            myLogInOrCreateFragment(Constants.toAccountFrag);
                        }
                        break;
                    case R.id.fab_search:
                        bookSearchView.setVisibility(View.VISIBLE);
                        bookSearchView.setIconified(false);
                        break;
                }
            }
        }
    };

    private void myLogInOrCreateFragment(String actionValue) {
        if (GlobalMethods.isGooglePlayServicesAvailable(this)) {
            LogInOrCreateFragment logInOrCreateFragment = new LogInOrCreateFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.actionString, actionValue);
            logInOrCreateFragment.setArguments(bundle);
            logInOrCreateFragment.show(getSupportFragmentManager(), "login_or_create");
        }
    }

    public void setVisibleSearchView(BookStoreAdapter bookStoreAdapter, List<String> bookTitleList) {
        fabSearch.setVisibility(View.VISIBLE);
        this.bookStoreAdapter=bookStoreAdapter;
        searchAutoComplete.setAdapter(new ArrayAdapter<>(this, R.layout.serache_item_view,R.id.search_text_view,bookTitleList ));
    }
    public void setInVisibleSearchView() {
        bookSearchView.setIconified(true);
        bookSearchView.setVisibility(View.GONE);
        fabSearch.setVisibility(View.GONE);
    }

    private void setButtonSelector(ImageButton imageButton){
        //lastButton set unpressed
        lastButton.setBackgroundResource(R.drawable.btn_unpressed);
        lastButton=imageButton;
        //newButton set pressed
        lastButton.setBackgroundResource(R.drawable.btn_pressed);
    }
    public void myBookStoreFragment() {
        setButtonSelector(btnBookRepositoryFrag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, new BookStoreFragment(), "store_fragment");
        fragmentTransaction.commit();
    }

    public void myAccountFragment() {
       setButtonSelector(btnAccountFrag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, new AccountFragment(),"account_fragment");
        fragmentTransaction.commit();
    }

    public void myPurchaseFragment() {
        setButtonSelector(btnMyPurchaseFrag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, new PurchasedBooksFragment());
        fragmentTransaction.commit();
    }

    public void myCartFragment() {
       setButtonSelector(btnMyCartFrag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, new CartFragment(),"cart_fragment");
        fragmentTransaction.commit();
    }

    public void myProjectsFragment() {
       setButtonSelector(btnMyProjectsFrag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, new ProjectsFragment(), "projects_fragment");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        ProjectsFragment projectsFragment = (ProjectsFragment) getSupportFragmentManager().findFragmentByTag("projects_fragment");
        if (projectsFragment != null && projectsFragment.isVisible()) {
            if (!projectsFragment.onBackPressed())
                super.onBackPressed();
        } else super.onBackPressed();
    }
    // Add Auth state listener in onStart method.
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // stop listener in onStop
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
