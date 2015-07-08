/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 * @author Bob Brodt
 ******************************************************************************/

package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.Assignment;
import org.eclipse.bpmn2.DataAssociation;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.ItemKind;
import org.eclipse.bpmn2.PotentialOwner;
import org.eclipse.bpmn2.ResourceAssignmentExpression;
import org.eclipse.bpmn2.ResourceRole;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
import org.eclipse.bpmn2.modeler.core.adapters.InsertionAdapter;
import org.eclipse.bpmn2.modeler.core.features.CustomElementFeatureContainer;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractBpmn2PropertySection;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractDetailComposite;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractListComposite;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.DefaultDetailComposite;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.DefaultListComposite;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.ListCompositeColumnProvider;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.ListCompositeContentProvider;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.TableColumn;
import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.BooleanObjectEditor;
import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.IntObjectEditor;
import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.NCNameObjectEditor;
import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.ObjectEditor;
import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.TextObjectEditor;
import org.eclipse.bpmn2.modeler.core.runtime.CustomTaskDescriptor;
import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor;
import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor.ModelExtensionAdapter;
import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor.Property;
import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Bob Brodt
 *
 */
public class JbpmTaskDetailComposite extends JbpmActivityDetailComposite {

	/**
	 * @param section
	 */
	public JbpmTaskDetailComposite(AbstractBpmn2PropertySection section) {
		super(section);
	}

	public JbpmTaskDetailComposite(Composite parent, int style) {
		super(parent, style);
	}
		
	@Override
	public void cleanBindings() {
		super.cleanBindings();
	}

	@Override
	protected boolean isModelObjectEnabled(String className, String featureName) {
		if ("DataInput".equals(className)) //$NON-NLS-1$
			return true;
		return super.isModelObjectEnabled(className, featureName);
	}

	@Override
	public void createBindings(EObject be) {
		createInputParameterBindings((Task)be);
		super.createBindings(be);
	}
	
	/**
	 * Create Object Editors for each of the Task's input parameters (DataInputs) that are
	 * defined in the extension plugin.xml
	 * 
	 * The mappings for these parameters are simply text expressions and will be rendered
	 * as individual editable fields on the Task property tab instead of being included in
	 * the I/O Parameters list.
	 * 
	 * @param task
	 */
	protected void createInputParameterBindings(final Task task) {
		
		// this may no longer be required since populateObject() is now called
		// in Bpmn2ModelerFactory.create(). See https://issues.jboss.org/browse/SWITCHYARD-2484
		// for details.
		ModelExtensionAdapter adapter = ModelExtensionDescriptor.getModelExtensionAdapter(task);
		if (adapter==null) {
			AddContext context = new AddContext(new AreaContext(), task);
			String id = CustomElementFeatureContainer.getId(context);
			if (id!=null) {
		    	TargetRuntime rt = TargetRuntime.getRuntime(task);
		    	CustomTaskDescriptor ctd = rt.getCustomTask(id);
		    	ctd.populateObject(task, task.eResource(), true);
		    	adapter = ModelExtensionDescriptor.getModelExtensionAdapter(task);
			}
		}
		
		if (adapter != null) {
//		ModelExtensionDescriptor med = BaseRuntimeExtensionDescriptor.getDescriptor(task, ModelExtensionDescriptor.class);
//		if (med != null) {
			// This Task object has <modelExtension> properties defined in the plugin.xml
			// check if any of the <property> elements extend the DataInputs or DataOutputs
			// (i.e. the I/O Parameter mappings) and create Object Editors for them.
			// If the Task does not define these parameter mappings, create temporary objects
			// for the editors (these will go away if they are not touched by the user)
//			List<Property> props = med.getProperties("ioSpecification/dataInputs/name"); //$NON-NLS-1$
			List<Property> props = adapter.getProperties("ioSpecification/dataInputs/name"); //$NON-NLS-1$
			InputOutputSpecification ioSpec = task.getIoSpecification();
			if (ioSpec==null) {
				ioSpec = createModelObject(InputOutputSpecification.class);
				InsertionAdapter.add(task,
						PACKAGE.getActivity_IoSpecification(),
						ioSpec);
			}
			
            Definitions definitions = ModelUtil.getDefinitions(task);
			for (Property property : props) {
				
				// this will become the label for the Object Editor
				final String name = property.getFirstStringValue();
				// the input parameter
				DataInput parameter = null;
				// the DataInputAssociation
				DataAssociation association = null;
				for (DataInput di : ioSpec.getDataInputs()) {
					if (name.equals(di.getName())) {
						// this is the one!
						parameter = di;
						for (DataAssociation da : task.getDataInputAssociations()) {
							if (da.getTargetRef() == di) {
								association = da;
								break;
							}
						}
						break;
					}
				}
				
				// create the DataInput element (the parameter) if needed
				if (parameter==null) {
                    ItemDefinition itemDef = createModelObject(ItemDefinition.class);
                    itemDef.setItemKind(ItemKind.INFORMATION);
                    itemDef.setStructureRef( ModelUtil.createStringWrapper("Object") );
                    InsertionAdapter.add(definitions,
                            PACKAGE.getDefinitions_RootElements(),
                            itemDef);
                    
					parameter = createModelObject(DataInput.class);
					parameter.setName(name);
					parameter.setItemSubjectRef(itemDef);
					InsertionAdapter.add(ioSpec,
							PACKAGE.getInputOutputSpecification_DataInputs(),
							parameter);
					
					// create the InputSet if needed
					InputSet inputSet = null;
					if (ioSpec.getInputSets().size()==0) {
						inputSet = createModelObject(InputSet.class);
						InsertionAdapter.add(ioSpec,
								PACKAGE.getInputOutputSpecification_InputSets(),
								inputSet);
					}
					else
						inputSet = ioSpec.getInputSets().get(0);
					// add the parameter to the InputSet also
					InsertionAdapter.add(inputSet,
							PACKAGE.getInputSet_DataInputRefs(),
							parameter);
				}
				
				// create the DataInputAssociation if needed
				if (association == null) {
					association = createModelObject(DataInputAssociation.class);
					association.setTargetRef(parameter);
					InsertionAdapter.add(task,
							PACKAGE.getActivity_DataInputAssociations(),
							association);
				}
				
				// create an MultipleAssignments and FormalExpression if needed
				// the "To" expression is the input parameter,
				// the "From" expression body is the target of the Object Editor
				FormalExpression fromExpression = null;
				Assignment assignment = null;
				if (association.getAssignment().size() == 1) {
					assignment = (Assignment) association.getAssignment().get(0);
					fromExpression = (FormalExpression) assignment.getFrom();
				}
				if (assignment==null) {
					assignment = createModelObject(Assignment.class);
					FormalExpression toExpression = createModelObject(FormalExpression.class);
					toExpression.setBody(parameter.getId());
					assignment.setTo(toExpression);
					InsertionAdapter.add(association, PACKAGE.getDataAssociation_Assignment(), assignment);
				}
				if (fromExpression==null) {
					fromExpression = createModelObject(FormalExpression.class);
					InsertionAdapter.add(assignment, PACKAGE.getAssignment_From(), fromExpression);
				}
				
				// create the Object Editor for the "From" expression body:
				// the data type is obtained from the DataInput <property> element from plugin.xml
				EAttribute attribute = PACKAGE.getFormalExpression_Body();
				String dataType = property.type;
				ObjectEditor editor = null;
				if ("EInt".equals(dataType)) { //$NON-NLS-1$
					editor = new IntObjectEditor(this,fromExpression,attribute);
				}
				else if ("EBoolean".equals(dataType)) { //$NON-NLS-1$
					editor = new BooleanObjectEditor(this,fromExpression,attribute) {
						@Override
						public Boolean getValue() {
							if (task instanceof UserTask && "Skippable".equals(name)) {
								// Sheesh! All this just to set the default value of
								// the User Task "Skippable" Data Input to true by default!
								UserTask ut = (UserTask) task;
								for (DataInput di : ut.getIoSpecification().getDataInputs()) {
									if ("Skippable".equals(di.getName())) {
										for (DataInputAssociation dia : ut.getDataInputAssociations()) {
											if (dia.getTargetRef() == di) {
												if (dia.getAssignment().size()==0) {
													return Boolean.TRUE;
												}
											}
										}
									}
								}
							}
							return super.getValue();
						}
					};
				}
				else if ("ID".equals(dataType)) { //$NON-NLS-1$
					editor = new NCNameObjectEditor(this,fromExpression,attribute);
				}
				else {
					editor = new TextObjectEditor(this,fromExpression,attribute);
					boolean isCDATA = "CDATA".equals(dataType);
					((TextObjectEditor)editor).setMultiLine(isCDATA);
				}
				editor.createControl(getAttributesParent(),ModelUtil.toCanonicalString(name));
			}
		}
	}

	@Override
	protected AbstractListComposite bindList(EObject object, EStructuralFeature feature, EClass listItemClass) {
		if (feature.getName().equals("resources")) { //$NON-NLS-1$
			if (isModelObjectEnabled(object.eClass(), feature)) {
				ActorsListComposite actors = new ActorsListComposite(this);
				actors.bindList(object, feature);
				actors.setTitle(Messages.JbpmTaskDetailComposite_Actors_Title);
				return actors;
			}
			return null;
		}
		else
			return super.bindList(object, feature, listItemClass);
	}
	
	public class ActorsNameTableColumn extends TableColumn {
		public ActorsNameTableColumn(EObject object) {
			super(object, PACKAGE.getFormalExpression_Body());
			setHeaderText(Messages.JbpmTaskDetailComposite_Actors_Name_Column);
			setEditable(true);
		}
	}
	
	public class ActorsListComposite extends DefaultListComposite {

		public ActorsListComposite(Composite parent) {
			super(parent, AbstractListComposite.DEFAULT_STYLE);
		}
		
		public EClass getListItemClass(EObject object, EStructuralFeature feature) {
			return PACKAGE.getFormalExpression();
		}
		
		@Override
		public ListCompositeColumnProvider getColumnProvider(EObject object, EStructuralFeature feature) {
			if (columnProvider==null) {
				columnProvider = new ListCompositeColumnProvider(this);
				columnProvider.add( new ActorsNameTableColumn(object) );
			}
			return columnProvider;
		}
		
		public ListCompositeContentProvider getContentProvider(EObject object, EStructuralFeature feature, EList<EObject>list) {
			if (contentProvider==null) {
				contentProvider = new ListCompositeContentProvider(this, object, feature, list) {
					@Override
					public Object[] getElements(Object inputElement) {
						List<Object> elements = new ArrayList<Object>();
						Task task = (Task)object;
						for (ResourceRole owner : task.getResources()) {
							ResourceAssignmentExpression resourceAssignment = owner.getResourceAssignmentExpression();
							if (resourceAssignment!=null) {
								Expression expression = resourceAssignment.getExpression();
								if (expression instanceof FormalExpression) {
									elements.add(expression);
								}
							}
						}
						return elements.toArray(); 
					}
				};
			}
			return contentProvider;
		}

		public AbstractDetailComposite createDetailComposite(Class eClass, Composite parent, int style) {
			AbstractDetailComposite composite = new DefaultDetailComposite(parent, style) {
				@Override
				protected Composite bindFeature(EObject be, EStructuralFeature feature, EClass eItemClass) {
					Composite composite = null;
					if (feature!=null && "body".equals(feature.getName())) { //$NON-NLS-1$
						super.bindFeature(be, feature, eItemClass);
					}
					return composite;
				}
				
				@Override
				protected void bindAttribute(Composite parent, EObject object, EAttribute attribute, String label) {
					TextObjectEditor editor = new TextObjectEditor(this,object,attribute);
					editor.setMultiLine(false);
					editor.createControl(parent,Messages.JbpmTaskDetailComposite_Actors_Name_Column);
				}
			};
			return composite;
		}

		protected EObject addListItem(EObject object, EStructuralFeature feature) {
			Task task = (Task)object;
			
			FormalExpression expression = createModelObject(FormalExpression.class);

			ResourceAssignmentExpression resourceAssignment = createModelObject(ResourceAssignmentExpression.class);
			resourceAssignment.setExpression(expression);
			PotentialOwner owner = createModelObject(PotentialOwner.class);
			owner.setResourceAssignmentExpression(resourceAssignment);
			task.getResources().add(owner);

			expression.setBody(Messages.JbpmTaskDetailComposite_Actors_Label);
			
			return expression;
		}
		
		protected Object removeListItem(EObject object, EStructuralFeature feature, int index) {
			Task task = (Task)object;
			int size = task.getResources().size();
			if (index>=0 && index<size) {
				task.getResources().remove(index);
			}
			return null;
		}

		@Override
		protected Object moveListItemUp(EObject object, EStructuralFeature feature, int index) {
			Task task = (Task)object;
			int size = task.getResources().size();
			if (index>0 && index<size) {
				ResourceRole owner = task.getResources().remove(index);
				task.getResources().add(index-1, owner);
				return owner;
			}
			return null;
		}

		@Override
		protected Object moveListItemDown(EObject object, EStructuralFeature feature, int index) {
			Task task = (Task)object;
			int size = task.getResources().size();
			if (index>=0 && index<size-1) {
				ResourceRole owner = task.getResources().remove(index);
				task.getResources().add(index, owner);
				return owner;
			}
			return null;
		}
	}
}
