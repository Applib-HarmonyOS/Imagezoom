[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=applibgroup_Imagezoom&metric=alert_status)](https://sonarcloud.io/dashboard?id=applibgroup_Imagezoom)

# ImageZoom
An Android library that makes any view to be zoomable.
It was created to mimick the Instagram Zoom feature.

![View Preview](https://github.com/okaybroda/ImageZoom/blob/master/preview.gif?raw=true)

### Dependency
1. For using ImageZoom module in sample app, include the source code and add the below dependencies in entry/build.gradle to generate hap/support.har.
```
	dependencies {
		implementation project(':imagezoom')
        	implementation fileTree(dir: 'libs', include: ['*.har'])
        	testCompile 'junit:junit:4.12'
	}
```
2. For using ImageZoom in separate application using har file, add the har file in the entry/libs folder and add the dependencies in entry/build.gradle file.
```
	dependencies {
		implementation fileTree(dir: 'libs', include: ['*.har'])
		testCompile 'junit:junit:4.12'
	}

```
3. For using ImageZoom from a remote repository in separate application, add the below dependencies in entry/build.gradle file.
```
	dependencies {
		implementation 'dev.applibgroup:imagezoom:1.0.0'
		testCompile 'junit:junit:4.12'
	}
```
## Features
* Double Tap to zoom or Pinch to zoom.
* Component can be dragged using one or two fingers.
* Double Tap the zoomed component to restore original component.

## Usage
Create an ImageZoomHelper instance in the OnCreate function of your AbilitySlice
```java
ImageZoomHelper imageZoomHelper;

@Override
public void onStart(Intent intent) {
	// ... your code ...
	imageZoomHelper = new ImageZoomHelper(this.getAbility());
}
```
Override onTouchEvent of the component to be zoomed and pass all of its touch events to the ImageZoomHelper instance, thereby enabling zoom:
```java
img.setTouchEventListener(new Component.TouchEventListener() {
	@Override
	public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
		// Request Focus here so that the latest component being touched is having the focus
                img.requestFocus();
		return imageZoomHelper.onDispatchTouchEvent((touchEvent)) || onTouchEvent(component, touchEvent);
	}
});
```
To enable/disable zoom for certain Views (e.g. Recycler View refreshing)

**NOTE:** Tags have not been implemented. But you can do any of the following according to usecase.

```java
// 1. Override setTouchEventListener according to your requirement if you want to disable zoom.
img.setTouchEventListener(new Component.TouchEventListener() {
	@Override
	public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
		return onTouchEvent(component, touchEvent);
	}
});

// 2. Removing that particular component out of focus.
img.setTouchFocusable(false);
```
### Advanced Usage
For a smoother zoom transition, set the layout to be fullscreen.
### ListContainer
Override the getComponent method in SampleItemProvider class.
```java
@Override
public Component getComponent(int position, Component convertComponent, ComponentContainer componentContainer) {
	final Component cpt;
	if (convertComponent == null) {
		cpt = LayoutScatter.getInstance(slice).parse(ResourceTable.Layout_item_sample, null, false);
	} else {
		cpt = convertComponent;
	}
	
	Component comp = cpt.findComponentById(ResourceTable.Id_comp);
	// Code for any kind of animation (if required).

	comp.setTouchEventListener(new Component.TouchEventListener() {
		@Override
		public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
			comp.requestFocus();
			return imageZoomHelper.onDispatchTouchEvent((touchEvent)) || onTouchEvent(component, touchEvent);
		}
	});

	return cpt;
}
```
