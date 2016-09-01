package uiuc.mbr;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import uiuc.mbr.activities.AddressBookActivity;
import uiuc.mbr.event_selection.AddressBook;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

/**
 * This test suite requires the tester to start with an empty addressBook since the Delete buttons
 * are not unique and cannot be easily chosen by espresso.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class AddLocationTest {

	private String string1 = "test";
	private String address1 = "502 W green st";
	private String string2 = "test 2";
	private String address2 = "509 bash ct";

	@Rule
	public ActivityTestRule<AddressBookActivity> mActivityRule = new ActivityTestRule<>(AddressBookActivity.class);

	/**
	 * Tests to see if an address can be added.
	 */
	@Test
	public void testAddNewLocation() {
		//add new name/location pair
		onView(withId(R.id.addAddress)).perform(click());
		onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(string1));
		onView(withText("OK")).perform(click());
		onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(address1));
		onView(withText("OK")).perform(click());
		Assert.assertNotNull(AddressBook.getByName(string1, InstrumentationRegistry.getTargetContext()));

		//cleanup
		onView(allOf(withText("Delete"))).perform(click());

	}

	/**
	 * Tests to see if it will not allow for a second saved address with the same name.
	 * Adds an name/address pair then tries to add a new address with the same name.
	 *
	 */
	@Test
	public void testAddExistingLocation() {
		//Add first address
		onView(withId(R.id.addAddress)).perform(click());
		onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(string1));
		onView(withText("OK")).perform(click());
		onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(address1));
		onView(withText("OK")).perform(click());

		//try to add second address
		onView(withId(R.id.addAddress)).perform(click());
		onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(string1));
		onView(withText("OK")).perform(click());
		onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).check(doesNotExist());

		//cleanup
		onView(allOf(withText("Delete"))).perform(click());
	}

}
