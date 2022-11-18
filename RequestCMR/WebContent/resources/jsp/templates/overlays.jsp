<!-- DIV for displaying an overlay with a dialog style -->
<div class="ibm-common-overlay" id="messagesOverlay">
	<div class="ibm-head">
		<p><a class="ibm-common-overlay-close" alt="">Close [x]</a></p>
	</div>
	<div class="ibm-body">
		<div class="ibm-main">
    <div id="alertTitleContainer" style="display:block">
			<div class="ibm-title">
            	<a id="overlayDynamicIcon" style="padding: 10px 5px 18px 38px !important" class="ibm-errorLarge-link" alt=""></a>
                <h1 class="ibm-alternate" id="messagesOverlayTitle" style="position: absolute;left: 70px; top: 22px">
					Stop
				</h1>
			</div>
      </div>
			<div class="ibm-container ibm-alternate">
				<div class="ibm-container-body">
					<p id="messagesOverlayContent" style ="padding-top: 30px">
					Content</p>
					<div class="ibm-overlay-rule"><hr /></div>
					<div class="ibm-buttons-row">
                    	<input id="messagesOverlayButtonOK" value="OK" type="button" name="ibm-cancel" style="float:right" class="ibm-btn-cancel-sec" onclick="cmr.hideAlert()"/>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="ibm-footer"></div>
</div>

<!-- DIV for displaying an overlay with a dialog style -->
<div class="ibm-common-overlay" id="dialogOverlay">
	<div class="ibm-head">
	<p><a class="ibm-common-overlay-close">Close [x]</a></p>
	</div>
	<div class="ibm-body">
		<div class="ibm-main">
<!-- 		<div class="ibm-title">
				<a id="dialogDynamicIcon" style="padding: 10px 5px 18px 38px !important" alt=""></a>
                <h1 class="ibm-alternate" id="dialogOverlayTitle" style="position: absolute;left: 75px; top: 5px">
					Confirm
				</h1>
			</div>-->
			<div class="ibm-container ibm-alternate">
				<div class="ibm-container-body">
					<p id="dialogOverlayContent"></p>
					<div class="ibm-overlay-rule"><hr /></div>
					<div class="ibm-buttons-row">
					<p>
							<input id="dialogOverlayButtonOK"  value="OK" type="button" name="ibm-continue" class="ibm-btn-arrow-pri"/>
							<span class="ibm-sep">&nbsp;</span>
							<input id="dialogOverlayButtonCancel"  value="Cancel" type="button" name="ibm-cancel" class="ibm-btn-cancel-sec" onclick="cmr.hideConfirm()"/>
							
							
					</p>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="ibm-footer"></div>
</div>
<!-- DIV for displaying an overlay with a progress bar -->
<div class="ibm-common-overlay" id="progressOverlay">
	<div class="ibm-body">
		<div class="ibm-main">
			<div class="ibm-title">
                <h1 class="ibm-alternate" id="progressOverlayTitle" style="text-align: center;">
					Loading, please wait...
				</h1>
			</div>
			<div class="ibm-container ibm-alternate">
				<div class="ibm-container-body">
					<p id="progressOverlayContent"><a class="ibm-spinner-large" href="#" onclick="return false" title="Dialog overlay content">&nbsp;</a></p>
				</div>
			</div>
		</div>
	</div>
	<div class="ibm-footer"></div>
</div>
